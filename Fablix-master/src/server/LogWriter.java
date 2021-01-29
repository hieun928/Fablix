package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {
	String fileName = "/home/ubuntu/servletLog.txt";
	String fileName2 = "/home/ubuntu/jdbcLog.txt";
	public LogWriter()
	{
		File file = new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File file2 = new File(fileName2);
		try {
			file2.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void writeServletTime(long servletTime)
	{
		writeToLog(fileName,servletTime);
	}
	
	public void writeJDBCTime(long jdbcTime)
	{
		writeToLog(fileName2,jdbcTime);
	}
	
	private void writeToLog(String fileInput,long time)
	{
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(fileInput,true);
			bw = new BufferedWriter(fw);
			bw.write(time + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
			if (bw != null)
					bw.close();
			if (fw != null)
				fw.close();
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
	
	}

	

}
