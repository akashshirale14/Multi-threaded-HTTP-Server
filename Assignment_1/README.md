# cs457-557-f19-pa1-akashshirale14
A Simple Multi-threaded HTTP Server

Steps to compile::
make 

Steps to run::
java Server

This assignment uses java Socket programming for communication.The server can send files to client in fomats like .html,.gif,.pdf,etc.
The www directory is also submitted with code.
If file not present in www directory Error 404 is displayed.
The application is done using java multithreading to have simultaneous downloads.
The shared data structure HashMap is used for access count.


Sample Input and its corresponding Output:
Input:: wget http://remote04.cs.binghamton.edu:44485/pdf-sample.pdf
Output: /pdf-sample.pdf|128.226.114.203|51852|1


Input:: wget http://remote04.cs.binghamton.edu:44485/yy.html
Output:: ERROR 404: Not Found

//when www diectory is removed
Input:: wget http://remote04.cs.binghamton.edu:37909/pdf-sample.pdf
Output:: Error: www directory not present

