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

package net.hamnaberg.json;

import net.hamnaberg.json.extension.Extended;
import net.hamnaberg.json.util.ListOps;
import net.hamnaberg.json.util.MapOps;
import net.hamnaberg.json.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Property extends Extended<Property> {
    Property(ObjectNode delegate) {
        super(delegate);
    }

    public String getName() {
        return delegate.get("name").asText();
    }

    public Optional<Value> getValue() {
        return ValueFactory.createValue(delegate.get("value"));
    }

    public Optional<String> getPrompt() {
        return delegate.has("prompt") ? Optional.some(delegate.get("prompt").asText()) : Optional.<String>none();
    }

    public boolean isArray() {
        return delegate.has("array");
    }

    public boolean isObject() {
        return delegate.has("object");
    }

    public List<Value> getArray() {
        JsonNode array = delegate.get("array");
        List<Value> builder = ListOps.newArrayList();
        if (array != null && array.isArray()) {
            for (JsonNode n : array) {
                Optional<Value> opt = ValueFactory.createValue(n);
                if (opt.isSome()) {
                    builder.add(opt.get());
                }
            }
        }
        return Collections.unmodifiableList(builder);
    }

    public Map<String, Value> getObject() {
        Map<String, Value> builder = MapOps.newHashMap();
        JsonNode object = delegate.get("object");
        if (object != null && object.isObject()) {
            Iterator<Map.Entry<String,JsonNode>> fields = object.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> next = fields.next();
                Optional<Value> opt = ValueFactory.createValue(next.getValue());
                if (opt.isSome()) {
                    builder.put(next.getKey(), opt.get());
                }
            }
        }
        return Collections.unmodifiableMap(builder);
    }

    @Override
    public String toString() {
        return String.format("Property with name %s, value %s, array %s, object %s, prompt %s", getName(), getValue().orNull(), getArray(), getObject(), getPrompt());
    }

    public static Property value(String name, Optional<String> prompt, Optional<Value> value) {
        ObjectNode node = makeObject(name, prompt);
        if (value.isSome()) {
            node.put("value", getJsonValue(value.get()));
        }
        return new Property(node);
    }

    public static Property array(String name, Optional<String> prompt, List<Value> list) {
        ObjectNode node = makeObject(name, prompt);
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (Value value : list) {
            array.add(getJsonValue(value));
        }
        node.put("array", array);
        return new Property(node);
    }

    public static Property object(String name, Optional<String> prompt, Map<String, Value> object) {
        ObjectNode node = makeObject(name, prompt);
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        for (Map.Entry<String, Value> entry : object.entrySet()) {
            objectNode.put(entry.getKey(), getJsonValue(entry.getValue()));
        }
        node.put("object", objectNode);
        return new Property(node);
    }

    @Override
    protected Property copy(ObjectNode value) {
        return new Property(value);
    }

    @Override
    public void validate() {

    }

    private static ObjectNode makeObject(String name, Optional<String> prompt) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("name", name);
        if (prompt.isSome()) {
            node.put("prompt", prompt.get());
        }
        return node;
    }

    private static JsonNode getJsonValue(Value value) {
        if (value.isNumeric()) {
            return new DoubleNode(value.asNumber().doubleValue());
        }
        else if (value.isString()) {
            return new TextNode(value.asString());
        }
        else if (value.isBoolean()) {
            return BooleanNode.valueOf(value.asBoolean());
        }
        return NullNode.getInstance();
    }

    public static List<Property> fromData(JsonNode data) {
        List<Property> builder = ListOps.newArrayList();
        for (JsonNode jsonNode : data) {
            builder.add(new Property((ObjectNode) jsonNode));
        }
        return Collections.unmodifiableList(builder);
    }

    public static Map<String, Object> toMap(List<Property> props) {
        Map<String, Object> builder = MapOps.newHashMap();
        for (Property prop : props) {
            if (prop.isObject()) {
                builder.put(prop.getName(), prop.getObject());
            }
            else if (prop.isArray()) {
                builder.put(prop.getName(), prop.getArray());
            }
            else {
                builder.put(prop.getName(), prop.getValue());
            }
        }

        return Collections.unmodifiableMap(builder);
    }
}
