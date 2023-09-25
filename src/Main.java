import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

public class Main{
        public static void main(String[] args) {
            try {
                new BackPropagation();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

}
class BackPropagation{
    public static final double LEANING_RATE = 0.0001;
    public static final double[] classA = {1, 0, 0};
    public static final double[] classB = {0, 1, 0};
    public static final double[] classC = {0, 0, 1};
    int count = 0;
    private ArrayList<Node> inputNode;
    private ArrayList<Node> outputNode;
    private Queue<Node> queueNode;
    private double[][] trainingData;
    private double[][] testData;
    double[] error;
    private int testCaseNum;
    private int hiddenNodeLevel;
    private int hiddenNodeNumByLevel;
    private boolean isTraining;
    private int hitNum = 0;
    public final double ReluDifferential(double output) {
        if (0<output)return 1;
        else return 0;
    }
    public final double ReluFunction(double input) {
        return Math.max(input, 0);
    }

    public BackPropagation() throws IOException{
        long beforeTime = System.currentTimeMillis();

        testCaseNum = 75;
        hiddenNodeNumByLevel = 5;
        hiddenNodeLevel = 2;
        int cycleNum = 50000;
        InputData();
        NeualNetworkDesign();
        isTraining =true;
        for(int i = 0; i<cycleNum; i++) {
            for(int j = 0; j<testCaseNum; j++) {
                //PrintNode();
                Calculating(j);
                //TestNetwork(j);
            }
            if (i%10000 == 0 ) System.out.println("진행률 : " + (double)i/cycleNum*100 + " %");
        }

        isTraining = false;
        for(int i = 0; i<testCaseNum; i++) {
            Calculating(i);
            TestNetwork(i);
        }
        System.out.println("정답 개수 = " + hitNum);
        System.out.println("정답률 : " + ((double)hitNum/testCaseNum)*100);
        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime)/1000;
        System.out.println("시간차이(s) : "+secDiffTime);
    }

    public void InputData() throws IOException{
        InitArrayList();
        BufferedReader br = new BufferedReader(new FileReader("./data/training.dat"));
        BufferedReader br2 = new BufferedReader(new FileReader("./data/testing.dat"));
        for(int i = 0; i<testCaseNum; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine());
            StringTokenizer st2 = new StringTokenizer(br2.readLine());
            for(int j = 0; j<4; j++) {
                trainingData[i][j] = Double.parseDouble(st.nextToken());
                testData[i][j] = Double.parseDouble(st2.nextToken());
            }
        }
    }

    public void InitArrayList() {
        inputNode = new ArrayList<>();
        outputNode = new ArrayList<>();
        queueNode = new LinkedList<>();
        trainingData = new double[testCaseNum][4];
        testData = new double[testCaseNum][4];
    }

    public void NeualNetworkDesign() {
        CreateInputNode();
        CreateHiddenNode();
        CreateOutputNode();
    }

    public void CreateInputNode() {
        for(int i = 0; i<4; i++) {
            Node newNode = new Node();
            newNode.number = count++;
            inputNode.add(newNode);
            queueNode.add(newNode);
        }
        ArrayList<Node> hiddenNode = new ArrayList<>();
        for(int j = 0; j<hiddenNodeNumByLevel; j++) {
            Node newHiddenNode = new Node();
            newHiddenNode.number = count++;
            hiddenNode.add(newHiddenNode);
            queueNode.add(newHiddenNode);
        }
        for(int j = 0; j<4; j++) {
            Node tmpNode = queueNode.poll();
            for(int k = 0; k<hiddenNodeNumByLevel; k++) {
                tmpNode.addLinkedFrontNodes(hiddenNode.get(k));
                tmpNode.adddWeightToNodes(Math.random()*(0.5-0.01)+0.01);
                hiddenNode.get(k).addLinkedPreNodes(tmpNode);
            }
        }
    }

    public void CreateHiddenNode() {
        for(int i = 0; i<hiddenNodeLevel-1; i++) {
            ArrayList<Node> hiddenNode = new ArrayList<>();
            for(int j = 0; j<hiddenNodeNumByLevel; j++) {
                Node newNode = new Node();
                newNode.number = count++;
                hiddenNode.add(newNode);
                queueNode.add(newNode);
            }
            for(int j = 0; j<hiddenNodeNumByLevel; j++) {
                Node tmpNode = queueNode.poll();
                for(int k = 0; k<hiddenNodeNumByLevel; k++) {
                    tmpNode.addLinkedFrontNodes(hiddenNode.get(k));
                    tmpNode.adddWeightToNodes(Math.random()*(0.5-0.01)+0.01);
                    hiddenNode.get(k).addLinkedPreNodes(tmpNode);
                }
            }
        }
    }

    public void CreateOutputNode() {
        for(int i = 0; i<3; i++) {
            Node newNode = new Node();
            newNode.number = count++;
            outputNode.add(newNode);
        }
        for(int i = 0; i<hiddenNodeNumByLevel; i++) {
            Node tmpNode = queueNode.poll();
            for(int j = 0; j<3; j++) {
                tmpNode.addLinkedFrontNodes(outputNode.get(j));
                tmpNode.adddWeightToNodes(Math.random()*(0.5-0.01)+0.01);
                outputNode.get(j).addLinkedPreNodes(tmpNode);
            }
        }
    }

    public void InitNodeResult() {
        for(int i = 0; i<4;i++) {
            queueNode.add(inputNode.get(i));
        }
        while(!queueNode.isEmpty()){
            Node node = queueNode.poll();
            node.setResult(0);
            node.setDelta(0);
            if(queueNode.isEmpty()) {
                for(int j = 0; j < node.getLinkedFrontNodes().size(); j++	) {
                    queueNode.add(node.getLinkedFrontNodes().get(j));
                }
            }
        }
    }

    public void Calculating(int testCase) {
        InitNodeResult();
        for(int i = 0; i<4;i++) {
            if(isTraining) {
                inputNode.get(i).setResult(trainingData[testCase][i]);
            }
            else {
                inputNode.get(i).setResult(testData[testCase][i]);
            }
            for(int j = 0 ; j<inputNode.get(i).getLinkedFrontNodes().size(); j++) {
                inputNode.get(i).getLinkedFrontNodes().get(j).setResult(inputNode.get(i).getLinkedFrontNodes().get(j).getResult()+
                        inputNode.get(i).getResult()*inputNode.get(i).getWeightToNodes().get(j));
            }
        }
        for(int i = 0; i<hiddenNodeNumByLevel; i++) {
            queueNode.add(inputNode.get(0).getLinkedFrontNodes().get(i));
        }
        while(!queueNode.isEmpty()){
            Node node = queueNode.poll();
            if (outputNode.contains(node))continue;

            node.setResult(ReluFunction(node.getResult()));

            for(int j = 0; j<node.getLinkedFrontNodes().size(); j++) {
                Node nextNode = node.getLinkedFrontNodes().get(j);
                nextNode.setResult(nextNode.getResult()+node.getResult()*node.getWeightToNodes().get(j));
            }
            if(queueNode.isEmpty()) {
                for(int j = 0; j < node.getLinkedFrontNodes().size(); j++) {
                    queueNode.add(node.getLinkedFrontNodes().get(j));
                }
            }
        }
        SoftMax();


        if(isTraining) {
            Learning(testCase);
        }
    }

    private void SoftMax() {
        double softMax = 0;
        for (Node node : outputNode) {
            softMax += Math.exp(node.getResult());
        }
        for (Node node : outputNode) {
            node.setResult(Math.exp(node.getResult()) / softMax);
        }
    }


    public void Learning(int testCase) {
        double[] testClass;
        if(testCase<25) {
            testClass = classA.clone();
        }else if(testCase<50) {
            testClass = classB.clone();
        }else {
            testClass = classC.clone();
        }
        error = new double[3];
        for(int i = 0; i<3; i++) {
            error[i] = outputNode.get(i).getResult()-testClass[i];
        }
        for(int i = 0; i<3; i++) {
            OutputNodeDelta(outputNode.get(i), i);
        }

        for(int i = 0; i<outputNode.get(0).getLinkedPreNodes().size(); i++) {
            queueNode.add(outputNode.get(0).getLinkedPreNodes().get(i));
        }
        while(!queueNode.isEmpty()) {
            Node node = queueNode.poll();

            /**
             * 드롭아웃
             */
            //if (new Random().nextDouble()<0.5) node.setResult(0);

            HiddenNodeDelta(node);
            if(queueNode.isEmpty()) {
                for(int i = 0; i< node.getLinkedPreNodes().size(); i++) {
                    queueNode.add(node.getLinkedPreNodes().get(i));
                }
            }
        }
        for(int i = 0; i<4; i++) {
            queueNode.add(inputNode.get(i));
        }
        while(!queueNode.isEmpty()) {
            Node node = queueNode.poll();
            ConnectionStrengthDifferential(node);

            if(queueNode.isEmpty()) {
                for(int i = 0; i< node.getLinkedFrontNodes().size(); i++) {
                    queueNode.add(node.getLinkedFrontNodes().get(i));
                }
            }
        }
    }

    public void OutputNodeDelta(Node node, int idx) {
        node.setDelta(error[idx]);
    }
    public void HiddenNodeDelta(Node node) {
        double hiddenNodeWDifferential = 0;
        for(int i = 0; i<node.getLinkedFrontNodes().size(); i++) {
            hiddenNodeWDifferential += node.getLinkedFrontNodes().get(i).getDelta()*node.getWeightToNodes().get(i);
        }


        hiddenNodeWDifferential *= ReluDifferential(node.getResult());
        node.setDelta(hiddenNodeWDifferential);
    }
    public void ConnectionStrengthDifferential(Node node) {
        for(int i = 0; i<node.getWeightToNodes().size(); i++) {
            double WDifferential = node.getLinkedFrontNodes().get(i).getDelta();
            WDifferential *= node.getResult();
            node.getWeightToNodes().set(i, (node.getWeightToNodes().get(i)-(LEANING_RATE * WDifferential)));
        }
    }

    public void TestNetwork(int testCase) throws IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        bw.write("테스트케이스 " + testCase + " 번째 \n");
        double maxResult = 0;
        int maxIdxResult = 0;
        for(int i = 0; i<3; i++) {
            if(outputNode.get(i).getResult()>maxResult) {
                maxIdxResult = i;
                maxResult = outputNode.get(i).getResult();
            }
        }
        for(int i = 0; i<3; i++) {
            bw.write("\nOutput " +  (i+1) + " 번째 결과 값 > " + outputNode.get(i).getResult());
        }
        bw.write("\n이것은 " + (char)(maxIdxResult+'A') + " 클래스입니다.");
        if(testCase<25) {
            if (maxIdxResult == 0) {
                bw.write("\n맞았습니다.");
                hitNum ++;
            }else{
                bw.write("\n틀렸습니다.");
            }
        }else if(testCase<50){
            if (maxIdxResult == 1) {
                bw.write("\n맞았습니다.");
                hitNum ++;
            }else{
                bw.write("\n틀렸습니다.");
            }
        }else{
            if (maxIdxResult == 2) {
                bw.write("\n맞았습니다.");
                hitNum ++;
            }else{
                bw.write("\n틀렸습니다.");
            }
        }
        bw.write("\n________________\n");
        bw.flush();
    }

    public void PrintNode() {
        for(int i = 0 ; i<4; i++) {
            queueNode.add(inputNode.get(i));
        }
        while(!queueNode.isEmpty()) {
            Node node = queueNode.poll();
            System.out.println("___" + node.number+ "____");
            System.out.println("델타 > " + node.getDelta());
            System.out.println("결과값 > " + node.getResult());
            for(int i = 0; i<node.getLinkedFrontNodes().size(); i++) {
                System.out.println(i + " 번째 연결강도> " + node.getWeightToNodes().get(i));
                System.out.println(node.getLinkedFrontNodes().get(i).number+ " 와 연결되어있음");
                System.out.println("그곳의 델타 > " + node.getLinkedFrontNodes().get(i).getDelta());
            }
            if(queueNode.isEmpty()) {
                for(int i = 0; i<node.getLinkedFrontNodes().size(); i++) {
                    queueNode.add(node.getLinkedFrontNodes().get(i));
                }
            }
        }
    }
}

class Node{
    int number;
    private double result;
    private ArrayList<Node> linkedFrontNodes;
    private ArrayList<Node> linkedPreNodes;
    private ArrayList<Double> weightToNodes;
    private double delta;

    public Node() {
        linkedFrontNodes = new ArrayList<>();
        linkedPreNodes = new ArrayList<>();
        weightToNodes = new ArrayList<>();
        result = 0;
    }

    public double getDelta() {
        return delta;
    }


    public void setDelta(double delta) {
        this.delta = delta;
    }

    public ArrayList<Node> getLinkedFrontNodes() {
        return linkedFrontNodes;
    }



    public void addLinkedFrontNodes(Node frontNode) {
        linkedFrontNodes.add(frontNode);
    }

    public ArrayList<Node> getLinkedPreNodes() {
        return linkedPreNodes;
    }

    public void addLinkedPreNodes(Node PreNode) {
        linkedPreNodes.add(PreNode);
    }
    public ArrayList<Double> getWeightToNodes() {
        return weightToNodes;
    }

    public void adddWeightToNodes(double weight) {
        weightToNodes.add(weight);
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

}
