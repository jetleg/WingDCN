import java.util.*;
public class Latency {
    static int generate = 0;//生成的总的新流量数
    static int generateTmp = 0;//每轮生成的新流量数
    static int count1 = 0;
    static int count2 = 0;
    static int countTmp = 0;
    //static int latency1 = 127;//ms
    //static int latency2 = 167;//ms
    final static double load = 1;
    static double PTL = 0.5;
    final static int M = 4;
    static int flowNum = M*M*M;
    static int portNum = M*M;
    static long cycleCount = 0;
    static long restNum = 0;
    static int[][] restFlow = new int[portNum][portNum];
    public static void main(String[] args) {
        for(int l = 0;l < 1000;l++){
            int[][] tmp = trafficMatrixGenerate(load,PTL,M);
            countThreeHop(tmp,M);
            //System.out.println(restNum+generateTmp+" "+(restNum+generateTmp-countTmp)+" "+countTmp);
            restNum = restNum+generateTmp - countTmp;
            cycleCount += restNum;
        }
        long count = count1+count2;
        double averageCycle = (double) cycleCount/generate;
        System.out.println(averageCycle+1);
    }
    public static int[][] trafficMatrixGenerate(double load,double PTL,int M){
        countTmp = 0;
        generateTmp = 0;
        //load负载率，M为每个AWGR的端口数
        int[][] F = new int[portNum][portNum];
        int[][] K = new int[portNum][portNum];
        int[][] FF = new int[portNum][portNum];
        int[][] Z = new int[portNum][portNum];
        //output[i]=j意味着ToRi的输出对应是ToRj
        int[] output = new int[flowNum];
        //初始化为flowNum+1，意味着没匹配任何输出ToR
        Arrays.fill(output,flowNum+1);
        Random rd = new Random();
        for(int i = 0;i < flowNum;i++){
            //通过tmp1和load比较，判断是否生成流
            double tmp1 = rd.nextDouble();
            if(tmp1 < load){
                generate++;
                generateTmp++;
                //通过tmp2和load比较，判断是否为intra-cluster
                double tmp2 = rd.nextDouble();
                int awgrIdx = i/M/M;
                //通过tmp
                if(tmp2 < PTL) output[i] = rd.nextInt(M*M)+1+M*M*awgrIdx;
                else{
                    int rand = rd.nextInt(flowNum)+1;
                    while((rand <= M*M*awgrIdx + M*M) && (rand >= M*M*awgrIdx+1))
                        rand = rd.nextInt(flowNum)+1;
                    output[i] = rand;
                }
            }
        }
        //F矩阵的生成
        for(int i = 0;i < flowNum;i++){
            if(output[i] != flowNum+1)
                F[(i-1)/M][(output[i]-1)/M]++;
        }
        for(int i = 0;i < portNum;i++){
            for(int j=0;j < portNum;j++){
                F[i][j]+=restFlow[i][j];
            }
        }
        //K矩阵的生成
        for(int i = 0;i < M*M;i++){
            int awgrIdx = i/M;
            for(int j = awgrIdx*M;j<awgrIdx*M+M;j++){
                K[i][j] = 1;
                if(F[i][j] > 0){
                    count1++;
                    countTmp++;
                }
            }
        }
        for(int i = 0;i < M*M;i++){
            for(int j = 0;j < M*M;j++){
                Z[i][j] = F[i][j] - K[i][j];
                if(Z[i][j] >= 0) FF[i][j] = Z[i][j];
            }
        }
        return FF;
    }
    public static void countThreeHop(int[][] FF, int M){
        int col = 0,row = 0;
        for(;row < portNum;row++){
            int awgrIdx = 0;
            for(;awgrIdx < M;awgrIdx++){
                for(col = awgrIdx*M;col < awgrIdx*M+M;col++){
                    if(FF[col][row] >= 1){
                        count2++;
                        countTmp++;
                        FF[col][row]--;
                        break;
                    }
                }
            }
        }
        for(int i = 0;i < FF.length;i++){
            restFlow[i] = Arrays.copyOf(FF[i],FF[i].length);
        }
    }
}