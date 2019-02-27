package com.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CmdExecutor {

   public static boolean executor(String executeStr) throws IOException, InterruptedException {
	   return executor(executeStr,"GB2312");
   }

	public static boolean executor(String executeStr,String character) throws IOException, InterruptedException {
   		System.out.println("执行命令："+executeStr);
		Process p = Runtime.getRuntime().exec(executeStr);
		StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),character);
		errorGobbler.start();

		StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),character);
		outGobbler.start();
		int executorResultCode = p.waitFor();
		System.out.println("进程执行返回值："+executorResultCode);
		if(executorResultCode!=0){
			System.out.println("操作系统错误代码0：成功\n操作系统错误代码1：操作不允许\n操作系统错误代码2：没有这样的文件或目录\n操作系统错误代码3：没有这样的过程\n" +
					"操作系统错误代码4：中断的系统调用\n操作系统错误代码5：输入/输出错误\n操作系统错误代码6：没有这样的设备或地址\n操作系统错误代码7：参数列表太长\n" +
					"操作系统错误代码8：执行格式错误\n操作系统错误代码9：坏的文件描述符\n操作系统错误代码10：无子过程");
		}
		return executorResultCode==0;
	}

	private static class StreamGobbler extends Thread {
		private InputStream is;
		private String character;

		public StreamGobbler(InputStream is, String character) {
			this.is = is;
			this.character = character;
		}

		public void run() {
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {
				isr = new InputStreamReader(is,character);
				br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally{
				try {
					br.close();
					isr.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}
