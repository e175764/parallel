import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
public class test {
//メイン関数
	public static void main(String[] args) {
		int p=1000000;
		int[] data1 = new int[p];
		boolean flag=false;
		int num_thread=5;


		try {
            // ファイルのパスを指定する
            File file = new File("radom.txt");
         
            // ファイルが存在しない場合に例外が発生するので確認する
            if (!file.exists()) {
                System.out.print("ファイルが存在しません");
                return;
            }
         
            // BufferedReaderクラスのreadLineメソッドを使って1行ずつ読み込み表示する
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String data;
            int k=p;
            while ((data = bufferedReader.readLine()) != null) {
                int i = Integer.parseInt(data);
                data1[k-1] = i;
                k--;
            }
         
            // 最後にファイルを閉じてリソースを開放する
            bufferedReader.close();
         
        } catch (IOException e) {
            e.printStackTrace();
    		}
    	int[] tmp = new int[p];
		if (flag) { //並列処理したい場合...
			int left, mid, right;
			left=0;
			right=p-1;
			mid=(left+right);
			long start = System.currentTimeMillis();
			parallel(num_thread,data1,tmp);
			merge(data1,tmp,left,mid,right);
			long end = System.currentTimeMillis();
			System.out.println("処理時間：" + (end - start) + " ms");
			m_sort(data1, tmp, 0, data1.length - 1);
			
		} else { //単一処理したい場合...
			long start = System.currentTimeMillis();
			m_sort(data1, tmp, 0, data1.length - 1);
			long end = System.currentTimeMillis();
			System.out.println("処理時間：" + (end - start) + " ms");
		}
	try{
		FileWriter file = new FileWriter("result.txt");
		PrintWriter pw = new PrintWriter(new BufferedWriter(file));
		for(int i=0;i<p;i++){
			String s = String.valueOf(data1[i]);
			pw.println(s);
		}
			pw.close();
		}catch (IOException e) {
      	  e.printStackTrace();
    	}	
	}
//マージソート関数
	static void m_sort(int[] data1, int[] tmp, int left, int right) {
	int mid;

	if (right > left) {
		mid = (right + left) / 2;
		m_sort(data1, tmp, left, mid);
		m_sort(data1, tmp, mid + 1, right);

		merge(data1, tmp, left, mid + 1, right);
	}
}
//マージ関数
	private static void merge(int[] data, int[] tmp, int left, int mid, int right) {
		int left_end = mid - 1;
		int tmp_pos = left;
		int num_elements = right - left + 1;
	
		// 2つのリストに要素が残っている
		while ((left <= left_end) && (mid <= right)) {
			if (data[left] <= data[mid]) {
				tmp[tmp_pos] = data[left];
				left++;
			} else {
				tmp[tmp_pos] = data[mid];
				mid++;
			}
			tmp_pos++;
		}

	// 左側のリスト
		while (left <= left_end) {
			tmp[tmp_pos] = data[left];
			left++;
			tmp_pos++;
		}
	
		// 右側のリスト
		while (mid <= right) {
			tmp[tmp_pos] = data[mid];
			mid++;
			tmp_pos++;
		}

	// 元の配列に格納
		for (int i = 0; i < num_elements; i++) {
			data[right] = tmp[right];
			right--;
		}
	}
//並列化関数
	static void parallel(int num,int[] data1, int[] tmp){
		//スレッドの枠の用意(num個)
		ExecutorService executor = Executors.newFixedThreadPool(num);
		 List<Future<?>> list = new ArrayList<Future<?>>();
		try{
			//別スレッドで順次処理タスクを渡す
			for(int i=0; i<num; i++)
			{
				int left = i * data1.length / num;
				int right = (i+1) * data1.length / num - 1;
				Future<?> future = executor.submit(new Tasks(data1,tmp,left,right));
    			list.add(future);
   		 	}
		}finally{
			//新規タスクの受付を終了して残ったタスクを継続する．
			executor.shutdown();
			/*
			try {
				//指定時間が経過するか，全タスクが終了するまで処理を停止する．
				executor.awaitTermination(100, TimeUnit.MINUTES);	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/

		}
		for (Future<?> future : list) {
 			try{
 				future.get();
 			}catch(InterruptedException | ExecutionException e){	
 			}
    	}
	}
}

public class Tasks implements Runnable{
	int[] data;
	int[] tmp;
	int left, right;

	public Tasks(int[] data, int[] tmp, int left, int right) {
		this.data = data;
		this.tmp = tmp;
		this.left = left;
		this.right = right;
	}

	@Override
	public void run() {
		test.m_sort(data, tmp, left, right);
	}
}