import java.util.*;
public class Main {
    static int generate = 0;
    static int count1 = 0;
    static int count2 = 0;
    static int latency1 = 127;//ms
    static int latency2 = 167;//ms
    final static double load = 1;
    static double PTL = 1;
    final static int M = 16;
    static int flowNum = M*M*M;
    static int portNum = M*M;
    public static void main(String[] args) {
        for(int l = 0;l < 1000;l++){
            int[][] tmp = trafficMatrixGenerate(load,PTL,M);
            countThreeHop(tmp,M);
        }
        long count = count1+count2;
        double latency = (double) ((long) count1*latency1+count2*latency2)/count;
        System.out.println("count1:"+count1+"count2:"+count2+"count:"+count);
        //System.out.println("PTL:"+PTL+"throughput:"+(double) count/generate);
        System.out.println("count:"+count+"generate:"+generate+"throughput:"+(double) count/generate+"latency:"+latency);
    }
    public static int[][] trafficMatrixGenerate(double load,double PTL,int M){
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
        //K矩阵的生成
        for(int i = 0;i < M*M;i++){
            int awgrIdx = i/M;
            for(int j = awgrIdx*M;j<awgrIdx*M+M;j++){
                K[i][j] = 1;
                if(F[i][j] > 0) count1++;
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
                        break;
                    }
                }
            }
        }
    }
}