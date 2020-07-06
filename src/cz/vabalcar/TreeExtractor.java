package cz.vabalcar;

interface TreeExtractor<K, TOut> {
    TOut extract(TreeNode<K> treeRoot);
}
