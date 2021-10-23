import java.io.*;
import java.util.*;


class HuffmanAlgorithm {

    public static final int ASCII = 256;

    public static void main(String[] args) throws IOException {


        Scanner sc= new Scanner(System.in);
        System.out.println("Press 1 to compress a file or Press 2 to decompress a file");
        String option = sc.nextLine();

        String cwd = System.getProperty("user.dir");
        cwd = cwd + "\\Practicals\\src\\HuffmanCompressionAssignment\\";
        System.out.println("Current working directory : " + cwd);

        if(option.equals("1")) {
            System.out.print("Enter a the location for the origin: ");
            String origin = cwd.toString() + sc.nextLine(); //reads string.
            System.out.println(origin);

            System.out.print("Enter a the location for the destination: ");
            String dest = cwd.toString() + sc.nextLine(); //reads string.

            long time = System.nanoTime();
            compress(origin,dest);
            System.out.print("Time taken to compress: " + (System.nanoTime() - time));
        }
        if(option.equals("2")) {
            System.out.print("Enter a the location for the destination: ");
            String nameOfFile = sc.nextLine();
            String dest = cwd.toString() + nameOfFile; //reads string.
            long time = System.nanoTime();
            huffmanDecompress(dest,nameOfFile);
            System.out.print("Time taken to decompress : " + (System.nanoTime() - time));
        }
    }

    /**
     * Reads in a txt file, counts the occurrences of each character and puts them in
     * a PQ
     * @return a PQ
     * @throws IOException
     */
    static PriorityQueue<Node> createPQ(String origin) throws IOException {


        Comparator<Node> numCompare = new Comparator<Node>() {  //this is a custom comparator that puts the least occurring chars at the top of the PQ (so they can be lower in the tree)
            @Override
            public int compare(Node o1, Node o2) {
                if (o1.getNum() > o2.getNum()) {
                    return 1;
                } else if (o1.getNum() < o2.getNum()) {
                    return -1;
                }
                return 0;
            }
        };

        PriorityQueue<Node> pq =
                new PriorityQueue<>(numCompare);


        File file = new File(origin);
        FileInputStream fileStream = new FileInputStream(file);
        InputStreamReader input = new InputStreamReader(fileStream);
        BufferedReader reader = new BufferedReader(input);

        String line;
        int[] charOccurrences = new int[ASCII]; //creates a temp map with the amount of chars in the ASCII alphabet as its size


        while ((line = reader.readLine()) != null) {
            int i = 0;
            while (i != line.length()) {
                charOccurrences[line.charAt(i)]++; //as chars can be interrupted as ints in relation to their index on the ascii table, we use the char as an index in the map
                i++;
            }
        }

        int j = 0;
        while (j != ASCII) {       //adds all the occurrences of the chars to the PQ (unless they didnt appear in the file)
            char character = (char) j;

            if(charOccurrences[j] == 0){
                j++;
                continue;
            }
            pq.add(new Node(String.valueOf(character),charOccurrences[j]));
            j++;
        }
        return pq;
    }

    public static void compress(String origin,String dest) throws IOException {
        Node tree = createHuffmanTree(createPQ(origin));    //creates the huffman tree via a priority Queue
        Map <Character,String> encoding = encoding(tree);   //creates a map that maps a character to its new binary value
        writeHuffmanTree(encoding,tree,origin,dest);        //writes the new binary value of the chars to a bin file thus compreessing it

    }

    /**
     * Reads in a bitstreamed huffman tree from a bin file to decode the binary values to a string
     * @param binaryIn  the object we use to read in binary values
     * @return root node of decoded tree
     */
    private static Node readHuffmanTree(BinaryIn binaryIn) {

        if (binaryIn.readBoolean() == true) {
            return new Node(Character.toString(binaryIn.readChar()),0 , null, null);
        }
        else {
            Node x = readHuffmanTree(binaryIn);
            Node y = readHuffmanTree(binaryIn);
            return new Node(null,0 ,x,y);
        }
    }

    /**
     *
     *  writes a bit-streamed huffman tree to a binary file
     * @param node the root node of the tree
     * @param binaryOut object we use to write to bin files
     */
    private static void writeHuffmanTreeNodes(Node node,BinaryOut binaryOut) {

        if(node.isLeaf()){
            binaryOut.write(true);
            binaryOut.write(node.getCharacter(),8); //inserts the regular ASCII value for that char next to its encoded binary value to tell the algorithm what this prefix
            return;                                    //corresponds to
        }

        binaryOut.write(false);
        writeHuffmanTreeNodes(node.getLeft(),binaryOut);
        writeHuffmanTreeNodes(node.getRight(),binaryOut);
    }


    /**
     *  This writes the initial huffman tree from a file's contents to a another file
     * @param encoded   the map used to assign chars to their new binary values
     * @param root      the root of the huffman tree
     * @param origin    the name of the file we are reading from
     * @param dest      the name of the file we are writing to
     * @throws IOException
     */
    public static void writeHuffmanTree(Map <Character,String> encoded,Node root,String origin,String dest) throws IOException {
        File originFilePath = new File(origin);
        String dst = dest;

        FileInputStream fileStream = new FileInputStream(originFilePath);
        InputStreamReader input = new InputStreamReader(fileStream);
        BufferedReader reader = new BufferedReader(input);

        BinaryOut binaryOut = new BinaryOut(dst);

        writeHuffmanTreeNodes(root,binaryOut); //writes the bit streamed tree to the file

        String line;
        StringBuilder fileToString = new StringBuilder();

        while ((line = reader.readLine()) != null) {   //reads in the contents of the file as a string
            fileToString.append(line);
        }


        for(char charInString : fileToString.toString().toCharArray()){ //looks the character from the file up in the map, then bit by bit writes its shortened binary value
            for(int i = 0 ; i < encoded.get(charInString).length();i++){
                if(encoded.get(charInString).charAt(i) == '1'){
                    binaryOut.write(true);
                }
                else if(encoded.get(charInString).charAt(i) == '0'){
                    binaryOut.write(false);
                }
            }
        }
        binaryOut.close();
    }

    /**
     * Decompress a bin file to a txt file
     * @param dst name of file we are decompressing
     * @throws IOException
     */
    public static void huffmanDecompress(String dst,String nameOfFile) throws IOException {

        InputStream inputStream = new FileInputStream(dst);
        BinaryIn binaryIn = new BinaryIn(inputStream);


        Node root = readHuffmanTree(binaryIn); //reads the bitstreamed huffman tree and creates a native one
        Node curr = root;
        StringBuilder concat = new StringBuilder();


        while (!binaryIn.isEmpty()){    //while the binaryIn is reading a 1 or a 0 (true or false)

            boolean value = binaryIn.readBoolean();

            if(value == true){ //equal to 1
                curr = curr.getRight();
            }

            if(value == false){ //equal to 0
                curr = curr.getLeft();
            }

            if(curr.isLeaf()){  //when a path has been traversed and is a node , we write out the contents
                concat.append(curr.getCharacter());
                curr = root;        // and then reset the curr node to the top of the huffman tree
            }
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(dst+".txt"))) {
            out.print(concat.toString());
        }
    }

    /**
     *  This creates a haspMap / lookup table with an ascii char and its new binary value (instead of its default 8 bit value)
     * @param node the root node of the huffman tree
     * @return a look up table
     */
    static Map <Character,String> encoding(Node node){
        Map <Character,String> map = new HashMap<>();
        encodingHelper(map,""  ,node);
        return map;
    }

    /**
     * recursive function to create entries in the hasp map
     * @param map
     * @param binaryVal the string binary value
     * @param curr  the current node
     */
    private static void encodingHelper(Map <Character,String> map, String binaryVal, Node curr){
        if(curr.isLeaf()){
            map.put(curr.getCharacter().charAt(0),binaryVal);  //when a node is a leaf node , we write its binary value into the map along with its char and stop traversing that path.
        }
        if(!curr.isLeaf()){  //traverses down the left and right side of a node
            encodingHelper(map,binaryVal + 0,curr.getLeft());
            encodingHelper(map,binaryVal + 1,curr.getRight());
        }
    }



    /**
     * This creates the inital huffmanTree
     * @param pq the Priority queue is the occurrences of each character in the file, where the higher the number of occurances, the higher the priority ( resulting in less bits allocated)
     * @return the root of the newly created huffman tree
     */
    static Node createHuffmanTree( PriorityQueue<Node> pq){

        while (pq.size() != 1){
            Node temp1 = pq.poll(); //assigns node then removes it from the PQ
            Node temp2 = pq.poll();


            if(temp1 != null && temp2 != null) {
                String value = temp1.getCharacter() + temp2.getCharacter(); //combines the first two entries in the top of the PQ and concats the chars and adds their frequencies
                int n = temp1.getNum() + temp2.getNum();
                pq.add(new Node(value, n, temp1, temp2));                   //then adds the resulting node to the PQ.
            }
            else if(temp1 != null && temp2 == null){
                int n = temp1.getNum();
                String value = temp1.getCharacter();
                pq.add(new Node(value, n, null,temp1));
            }
            else if(temp1 == null && temp2 != null){
                String value = temp2.getCharacter();
                int n = temp2.getNum();
                pq.add(new Node(value, n, null, temp2));
            }
        }
        return pq.poll(); //returns the root of the tree
    }

}