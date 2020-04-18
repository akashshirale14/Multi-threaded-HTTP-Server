import java.util.*;
import java.net.InetAddress;
import java.net.Socket;
import java.io.*;
import java.io.PrintWriter;
import java.util.Date;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.net.ServerSocket;
import java.net.URLConnection;
import java.net.FileNameMap;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.io.UnsupportedEncodingException;
import java.io.BufferedOutputStream;
import java.nio.charset.StandardCharsets;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.text.SimpleDateFormat;
public class Server implements Runnable
{

    private Socket s;
    private int port;
    private String ipString;
    private static  Map<String,Integer>hm=new HashMap<>();
    public Server(Socket connect,int portNum,String ip) {
        s = connect;
        port=portNum;
        ipString=ip;
    }


    public static void main(String[] args)
    {
        try
        {   
            ServerSocket ss = new ServerSocket(0);
            InetAddress in = InetAddress.getLocalHost();
            //System.out.println("Host Address:- "+in.getHostAddress());
            System.out.println("Host Name:- " + in.getCanonicalHostName());
            System.out.println("Port Number:- " +ss.getLocalPort());
            String gettingURL="http://"+in.getCanonicalHostName()+":"+ss.getLocalPort();
            System.out.println(gettingURL);
           
            while (true)
            {
                Socket conn=ss.accept();
                int clientPort=conn.getPort();
                String ipString=Server.getIPMethod(conn);
                Server  se= new Server(conn,clientPort,ipString);
                //Making new thread for new Socket connection
                Thread t=new Thread(se);
                t.start();   

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }  

//Runs for each individual thread
    public void run(){
            
            try{     
                BufferedOutputStream dataOut=null;
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                int count = 0;
                    
               String  url = br.readLine();
                String currentDirectory = System.getProperty("user.dir");
                
                currentDirectory += "/www";
                //if www directory not present
                File www=new File(currentDirectory);
                if(www.exists()==false){
                    System.out.println("Error: www directory not present");
                    System.exit(1);
                }
                int flag = 0;
                String[] gettingFilePath = url.split(" ");
                String gettingMIME = gettingFilePath[1];
                //locates file with /foo/bar.html
                String[] absolutefile = gettingMIME.split("/");
                String ansMIME = absolutefile[absolutefile.length - 1];

                String finalFilePath = gettingFilePath[1];
                String forErrorFile=currentDirectory;
                currentDirectory += finalFilePath;
                
                File f = new File(currentDirectory);
                PrintWriter pw = new PrintWriter(s.getOutputStream());
                
                Date date = new Date();
                
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String mimeType=null;
            
                mimeType = fileNameMap.getContentTypeFor(f.getName());
  
                if(mimeType==null){
                    //for octet file
                    mimeType="application/octet-stream";
                }
                if(mimeType.compareTo("text/html") == 0)
                {
                    flag = 1;
                }
                if(mimeType.compareTo("application/octet-stream")==0){
                    flag=2;
                }
                //if file is present in www directory
                if(f.exists() == true)
                {
                    pw.println("HTTP/1.1 200 OK");
                    SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));  
                    SimpleDateFormat dateFormatLocal = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    
                    pw.println(dateFormatGmt.format(new Date()));
                    String serverName = "Server: " + "Akash's Server";
                    pw.println(serverName);
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date d = new Date(f.lastModified());

                    pw.println(sdf.format(d));
                   
                    Path pdfPath = Paths.get(currentDirectory);
                    byte[] fileContent = Files.readAllBytes(pdfPath);
                    String converted = "";
                    if(flag == 1)
                    {
                        //for html files
                        converted = new String(fileContent, StandardCharsets.UTF_8);
                        int num = converted.length();
                        String numlength = "Content-Length: " + String.valueOf(num); //;
                        pw.println(numlength);
                    }else if(flag==2){
                        //for application/octet files
                        int fileLength = fileContent.length;
                        //System.out.println(fileLength);
                        String numlength2 = "Content-Length: " + String.valueOf(fileLength); //;
                        pw.println(numlength2);
                        url="";
                    }
                    else
                    {
                        //for other files
                        int fileLength = fileContent.length;
                        String numlength2 = "Content-Length: " + String.valueOf(fileLength); //;
                        pw.println(numlength2);
                    }
                    //data structure for handling access count
                    int accesscount =Server.putValuesInMap(gettingMIME);
                    String content_Type = "Content-Type: " + mimeType;
                    pw.println(content_Type);
                    pw.println();
                    
                    if(flag == 1)
                    {
                        pw.println(converted);
                        pw.flush();
                    }
                    else {                        
                        //gif getting read
                    	dataOut = new BufferedOutputStream(s.getOutputStream());
                    	int fileLength=(int)f.length();	
                        pw.flush();
                    	dataOut.write(fileContent, 0,fileLength);
						dataOut.flush();
                        dataOut.close();
                    }

                 System.out.println(gettingMIME+"|"+ipString+"|"+port+"|"+accesscount);  
                }else{
                    //if file not present return 404 error
                    pw.println("HTTP/1.1 404 Not Found");
                    pw.println(date);
                    String a="Error 404:File not Found";
                    pw.println("Content-Length: 24");
                    pw.println("Content-Type: "+"text/plain");
                    pw.println("");
                    pw.println("Error 404:File not Found");
                    pw.flush();
                }
                //close socket connection
                s.close();
                url="";
            }catch(Exception e){
            e.printStackTrace();
        }
    }

//returns the access count using HashMap data structure
    public static synchronized int putValuesInMap(String dir){
        int count=0;
        if(hm.get(dir)==null){
            hm.put(dir,1);
            return 1;
        }else{
             count=hm.get(dir);
            hm.put(dir,count+1);
        }
        return count+1;
    }

//returns the IP of the client
    public static String getIPMethod(Socket conn){
        SocketAddress socketAddress = conn.getRemoteSocketAddress();
            if (socketAddress instanceof InetSocketAddress) {
            InetAddress inetAddress = ((InetSocketAddress)socketAddress).getAddress();
            if (inetAddress instanceof Inet4Address){
                String x=String.valueOf(inetAddress);
                String[] ans=x.split("/");
                 return ans[1];
            }else if (inetAddress instanceof Inet6Address){
                String x=String.valueOf(inetAddress);
                String[] ans=x.split("/");
                return ans[1];
            }else
                return "Not an IP address.";
            } else {
             return "Not an internet protocol socket.";
            }
    }

}