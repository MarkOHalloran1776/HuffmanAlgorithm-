import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.SortedSet;

public class Node {

    private String character = "";
    private int num;
    private Node left = null;
    private Node right = null;

    public Node(String c, int n) {
        character += c;
        num = n;
    }

    public Node(String c, int n,Node left, Node right) {
        character += c;
        num = n;
        this.left = left;
        this.right = right;
    }

    public Node(int n,Node left, Node right) {
        //character += c;
        num = n;
        this.left = left;
        this.right = right;
    }

    public String toString() {
        String entry = "[ " + character + " : " + num + "]";
        return entry;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight(){
        return right;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public boolean isLeaf(){
        return left == null && right == null;
    }

    public int getNum() {
        return num;
    }

    public void incr(){
        this.num++;
    }

    public void decr(){
        this.num--;
    }

    public String getCharacter() {
        return character;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    static void printPostorder(Node node)
    {
        // first recur on left subtree
        printPostorder(node.left);

        // then recur on right subtree
        printPostorder(node.right);

        // now deal with the node
        System.out.print(node.character + " ");
    }

}