package cz.vabalcar;

import java.util.HashMap;
import java.util.Map;

class TreeNode<K> {
    private Map<K, Object> attributes = new HashMap<>();
    public Map<K, Object> getAttributes() {
        return attributes;
    }
    public <T> void addAttribute(K attributeKey, T attributeValue) {
        attributes.put(attributeKey, attributeValue);
    }
    public void addAttributes(Map<? extends K, ?> attributes) {
        this.attributes.putAll(attributes);
    }
    @SuppressWarnings("unchecked")
    public <T> T getAttributeValue(Class<T> attributeValueClass, K attributeKey) {
        Object value = attributes.get(attributeKey);
        return (T)value;
    }

    @Override
    public String toString() {
        return attributes.toString();
    }
}
