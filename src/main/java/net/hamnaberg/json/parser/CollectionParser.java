/*
 * Copyright 2012 Erlend Hamnaberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.hamnaberg.json.parser;

import com.fasterxml.jackson.core.TreeNode;
import net.hamnaberg.json.*;
import net.hamnaberg.json.Collection;
import net.hamnaberg.json.util.Charsets;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;

/**
 * Parser for a vnd.collection+json document.
 */
public class CollectionParser {

    private final JsonFactory factory = new JsonFactory(new ObjectMapper());

    public Collection parse(Reader reader) throws IOException {
        try {
            JsonParser jsonParser = factory.createJsonParser(reader);
            return parse(jsonParser.readValueAsTree());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Parses a Collection from the given stream.
     * The stream is wrapped in a BufferedReader.
     * <p/>
     * The stream is expected to be UTF-8 encoded.
     *
     * @param stream the stream
     * @return a Collection
     * @throws IOException
     */
    public Collection parse(InputStream stream) throws IOException {
        return parse(new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8)));
    }

    /**
     * Parses a Collection from the given String.
     *
     * @param input the string to parse
     * @return a Collection
     * @throws IOException
     */
    public Collection parse(String input) throws IOException {
        return parse(new StringReader(input));
    }

    public Template parseTemplate(Reader reader) throws IOException {
        try {
            return parseTemplate(factory.createJsonParser(reader).readValueAsTree());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Parses a JsonCollection from the given stream.
     * The stream is wrapped in a BufferedReader.
     * <p/>
     * The stream is expected to be UTF-8 encoded.
     *
     * @param stream the stream
     * @return a jsonCollection
     * @throws IOException
     */
    public Template parseTemplate(InputStream stream) throws IOException {
        return parseTemplate(new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8)));
    }

    public Template parseTemplate(String input) throws IOException {
        return parseTemplate(new StringReader(input));
    }

    private Collection parse(TreeNode node) throws IOException {
        if (node instanceof ObjectNode) {
            ObjectNode obj = (ObjectNode) node;
            JsonNode collectionNode = obj.get("collection");
            return parseCollection(collectionNode);
        }
        throw new IllegalArgumentException("Illegal JSON here");
    }

    private Collection parseCollection(JsonNode collectionNode) {
        Collection c = objectFactory.createCollection((ObjectNode) collectionNode);
        c.validate();
        return c;
    }

    private Template parseTemplate(TreeNode node) {
        if (node instanceof ObjectNode) {
            ObjectNode obj = (ObjectNode) node;
            JsonNode template = obj.get("template");
            if (template != null) {
                return objectFactory.createTemplate((ObjectNode) template);
            }
        }
        throw new IllegalArgumentException("Illegal JSON here");
    }

    private static InternalObjectFactory objectFactory = new InternalObjectFactory() {
    };
}
