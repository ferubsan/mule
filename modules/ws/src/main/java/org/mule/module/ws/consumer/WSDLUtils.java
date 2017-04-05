/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.ws.consumer;

import static org.mule.module.http.api.HttpConstants.Protocols.HTTP;

import org.mule.util.StringUtils;
import org.mule.util.xmlsecurity.XMLSecureFactories;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WSDLUtils
{

    private static final String XML_NS_PREFIX = "xmlns:";
    private static final String XML_IMPORT_ELEMENT = "import";
    private static final String XML_INCLUDE_ELEMENT = "include";

    /**
     * Returns all the XML schemas from a WSDL definition.
     *
     * @throws TransformerException If unable to transform a Schema into String.
     */
    public static List<String> getSchemas(Definition wsdlDefinition) throws TransformerException
    {
        return getSchemas(wsdlDefinition, new ArrayList<String>(), new ArrayList<String>());
    }

    private static List<String> getSchemas(Definition wsdlDefinition, List<String> alreadyImportedSchemas, List<String> alreadyIncludedSchemas) throws TransformerException
    {
        Map<String, String> wsdlNamespaces = wsdlDefinition.getNamespaces();
        List<String> schemas = new ArrayList<String>();
        List<Types> typesList = new ArrayList<Types>();

        // Add current types definition if present
        if (wsdlDefinition.getTypes() != null)
        {
            typesList.add(wsdlDefinition.getTypes());
        }

        for (Types types : typesList)
        {
            for (Object o : types.getExtensibilityElements())
            {
                if (o instanceof javax.wsdl.extensions.schema.Schema)
                {
                    addSchema(wsdlNamespaces, schemas, (Schema) o, alreadyImportedSchemas, alreadyIncludedSchemas);
                }
            }
        }

        // Allow importing types from other wsdl
        for (Object wsdlImportList : wsdlDefinition.getImports().values())
        {
            for (Import wsdlImport : (List<Import>) wsdlImportList)
            {
                schemas.addAll(getSchemas(wsdlImport.getDefinition(), alreadyImportedSchemas, alreadyIncludedSchemas));
            }
        }

        return schemas;
    }

    private static void addSchema(Map<String, String> wsdlNamespaces, List<String> schemas, Schema schema, List<String> alreadyImportedSchemas, List<String> alreadyIncludedSchemas)
            throws TransformerException
    {
        for (Map.Entry<String, String> entry : wsdlNamespaces.entrySet())
        {
            boolean isDefault = StringUtils.isEmpty(entry.getKey());
            boolean containsNamespace = schema.getElement().hasAttribute(XML_NS_PREFIX + entry.getKey());

            if (!isDefault && !containsNamespace)
            {
                schema.getElement().setAttribute(XML_NS_PREFIX + entry.getKey(), entry.getValue());
            }
        }

        removeIncludesAndImportsFrom(schema);
        addSchemaImports(wsdlNamespaces, schemas, schema, alreadyImportedSchemas, alreadyIncludedSchemas);
        addSchemaIncludes(wsdlNamespaces, schemas, schema, alreadyImportedSchemas, alreadyIncludedSchemas);

        schemas.add(schemaToString(schema));
    }

    private static void addSchemaImports(Map<String, String> wsdlNamespaces, List<String> schemas, Schema schema, List<String> alreadyImportedSchemas, List<String> alreadyIncludedSchemas)
            throws TransformerException
    {
        for (Object imp : schema.getImports().values())
        {
            for (SchemaImport schemaImport : (java.util.Vector<SchemaImport>) imp)
            {
                if (alreadyImportedSchemas.contains(schemaImport.getSchemaLocationURI()))
                {
                    continue;
                }
                alreadyImportedSchemas.add(schemaImport.getSchemaLocationURI());
                addSchema(wsdlNamespaces, schemas, schemaImport.getReferencedSchema(), alreadyImportedSchemas, alreadyIncludedSchemas);
            }
        }
    }

    private static void addSchemaIncludes(Map<String, String> wsdlNamespaces, List<String> schemas, Schema schema, List<String> alreadyImportedSchemas, List<String> alreadyIncludedSchemas)
            throws TransformerException
    {
        for (Object item : schema.getIncludes())
        {
            SchemaReference schemaInclude = (SchemaReference) item;
            if (alreadyImportedSchemas.contains(schemaInclude.getSchemaLocationURI()))
            {
                continue;
            }
            alreadyIncludedSchemas.add(schemaInclude.getSchemaLocationURI());
            addSchema(wsdlNamespaces, schemas, schemaInclude.getReferencedSchema(), alreadyImportedSchemas, alreadyIncludedSchemas);
        }
    }

    /**
     * Removes includes and imports from schema as they were already stored
     * in the WSDL definition.
     */
    private static void removeIncludesAndImportsFrom(Schema schema)
    {
        Collection<List<SchemaReference>> schemaImportsCollection = schema.getImports().values();
        Collection<SchemaReference> schemaIncludesCollection = schema.getIncludes();

        if (!schemaImportsCollection.isEmpty() || !schemaIncludesCollection.isEmpty())
        {
            // remove includes and imports from dom so that they are not retrieved again
            NodeList children = schema.getElement().getChildNodes();
            for (int i = 0; i < children.getLength(); i++)
            {
                Node item = children.item(i);
                if (XML_IMPORT_ELEMENT.equals(item.getLocalName()) || XML_INCLUDE_ELEMENT.equals(item.getLocalName()))
                {
                    item.getParentNode().removeChild(item);
                }
            }
        }
    }

    /**
     * Returns the base path from an URL string
     * 
     * @param documentURI URL string
     * @return base path
     */
    public static String getBasePath(String documentURI)
    {
        File document = new File(documentURI);
        if (document.isDirectory())
        {
            return documentURI;
        }

        String fileName = document.getName();
        int fileNameIndex = documentURI.lastIndexOf(fileName);
        if (fileNameIndex == -1)
        {
            return documentURI;
        }

        return documentURI.substring(0, fileNameIndex);
    }


    /**
     * Converts a schema into a String.
     * @throws TransformerException If unable to transform the schema.
     */
    public static String schemaToString(Schema schema) throws TransformerException
    {
        StringWriter writer = new StringWriter();
        Transformer transformer = XMLSecureFactories.createDefault().getTransformerFactory().newTransformer();
        transformer.transform(new DOMSource(schema.getElement()), new StreamResult(writer));
        return writer.toString();
    }


    /**
     * Retrieves the list of SOAP body parts of a binding operation, or null if there is no
     * SOAP body defined.
     */
    public static List<String> getSoapBodyParts(BindingOperation bindingOperation)
    {
        List extensions = bindingOperation.getBindingInput().getExtensibilityElements();
        List<String> result = null;
        boolean found = false;

        for (Object extension : extensions)
        {
            if (extension instanceof SOAPBody)
            {
                result = ((SOAPBody) extension).getParts();
                found = true;
                break;
            }
            if (extension instanceof SOAP12Body)
            {
                result = ((SOAP12Body) extension).getParts();
                found = true;
                break;
            }
        }

        if (found && result == null)
        {
            result = Collections.emptyList();
        }

        return result;
    }

    /**
     * Retrieves the SOAP version of a WSDL binding, or null if it is not a SOAP binding.
     */
    public static SoapVersion getSoapVersion(Binding binding)
    {
        List extensions = binding.getExtensibilityElements();
        for (Object extension : extensions)
        {
            if (extension instanceof SOAPBinding)
            {
                return SoapVersion.SOAP_11;
            }
            if (extension instanceof SOAP12Binding)
            {
                return SoapVersion.SOAP_12;
            }
        }
        return null;
    }

}
