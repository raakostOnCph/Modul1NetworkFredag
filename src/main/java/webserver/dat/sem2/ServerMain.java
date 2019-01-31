package webserver.dat.sem2;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map.Entry;

/**
 The purpose of ServerMain is to...

 @author kasper
 */
public class ServerMain {

    public static void main( String[] args ) throws Exception {
        picoServer06();
    }

    /*
    Plain server that just answers what date it is.
    It ignores all path and parameters and really just tell you what date it is
     */
    private static void picoServer01() throws Exception {
        final ServerSocket server = new ServerSocket( 65080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        while ( true ) { // spin forever } }
            try ( Socket socket = server.accept() ) {
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            }

        }
    }

    /*
    Same server, but this one writes to system.out to show what info we get
    from the browser/client when we it sends a request to the server.
    It still just tell the browser what time it is.
     */
    private static void picoServer02() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        while ( true ) { // keep listening (as is normal for a server)
            try ( Socket socket = server.accept() ) {
                System.out.println( "-----------------" );
                System.out.println( "Client: " + socket.getInetAddress().getHostName() );
                System.out.println( "-----------------" );
                BufferedReader br = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
                String line;
                while ( !( ( line = br.readLine() ).isEmpty() ) ) {
                    System.out.println( line );
                }
                System.out.println( ">>>>>>>>>>>>>>>" );
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                System.out.println( "<<<<<<<<<<<<<<<<<" );
            }

        }
    }

    /*
    This server uses a HttpRequest object to *parse* the text-request into a
    java object we can then use to examine the different aspect of the request
    using the getters of the HttpRequest object.
    It still just returns the date to the client.
     */
    private static void picoServer03() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        int count = 0;
        while ( true ) { // keep listening (as is normal for a server)
            try ( Socket socket = server.accept() ) {
                System.out.println( "---- Request: " + count++ + " --------" );
                HttpRequest req = new HttpRequest( socket.getInputStream() );

                System.out.println( "Method: " + req.getMethod() );
                System.out.println( "Protocol: " + req.getProtocol() );
                System.out.println( "Path: " + req.getPath() );
                System.out.println( "Parameters:" );
                for ( Entry e : req.getParameters().entrySet() ) {
                    System.out.println( "    " + e.getKey() + ": " + e.getValue() );
                }
                System.out.println( "Headers:" );
                for ( Entry e : req.getHeaders().entrySet() ) {
                    System.out.println( "    " + e.getKey() + ": " + e.getValue() );
                }


                System.out.println( "---- BODY ----" );
                System.out.println( req.getBody() );
                System.out.println( "==============" );
                Date today = new Date();
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + today;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            }
        }
    }

    /*
    This server uses the path of the HttpRequest object to return a html file to
    the browser. See the notes on Java ressources.
     */
    private static void picoServer04() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
        while ( true ) { // keep listening (as is normal for a server)
            try ( Socket socket = server.accept() ) {
                System.out.println( "-----------------" );
                HttpRequest req = new HttpRequest( socket.getInputStream() );
                String path = root + req.getPath();
                String html = getResourceFileContents( path );
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                System.out.println( "<<<<<<<<<<<<<<<<<" );
            }
        }
//        System.out.println( getFile("adding.html") );
    }

    /*
    This server has exception handling - so if something goes wrong we do not
    have to start it again. (this is a yellow/red thing for now)
     */
    private static void picoServer05() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
        while ( true ) { // keep listening (as is normal for a server)
            Socket socket = server.accept();;
            try {
                System.out.println( "-----------------" );
                HttpRequest req = new HttpRequest( socket.getInputStream() );
                String path = root + req.getPath();
                String html = getResourceFileContents( path );
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                System.out.println( "<<<<<<<<<<<<<<<<<" );
            } catch ( Exception ex ) {
                String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                        + "UUUUPS: " + ex.getLocalizedMessage();
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            } finally {
                if ( socket != null ) {
                    socket.close();
                }
            }
        }
//        System.out.println( getFile("adding.html") );
    }

    /*
    This server requires static files to be named ".html" or ".txt". Other path
    names is assumed to be a name of a service.
     */
    private static void picoServer06() throws Exception {
        final ServerSocket server = new ServerSocket( 8080 );
        System.out.println( "Listening for connection on port 8080 ...." );
        String root = "pages";
//        String root = "pages";
        int count = 0;
        while ( true ) { // keep listening (as is normal for a server)
            Socket socket = server.accept();
            try {
                System.out.println( "---- reqno: " + count + " ----" );
                HttpRequest req = new HttpRequest( socket.getInputStream() );
                String path = req.getPath();
                if ( path.endsWith( ".html" ) || path.endsWith( ".txt" ) ) {
                    String html = getResourceFileContents( root+path );
                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + html;
                    socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                } else {
                    String res = "";
                    switch ( path ) {
                        case "/addournumbers":
                            if (req.getParameter("operator").equalsIgnoreCase("add")) {
                                res = addOurNumbers(req);
                                break;
                            }
                            if (req.getParameter("operator").equalsIgnoreCase("mul")){
                                res = mulOurNumbers(req);
                                break;
                            }
                            if (req.getParameter("operator").equalsIgnoreCase("div")){
                                res = divOurNumbers(req);
                                break;
                            }
                            if (req.getParameter("operator").equalsIgnoreCase("minus")){
                                res = minusOurNumbers(req);
                                break;
                            }
                        case"/calculate":
                            res= calOurNumbers(req);
                            break;
                        case "/getAllParameters":
                            res = printAllParameters(req);
                            break;
                        default:
                            res = "Unknown path: " + path;
                    }
                    String httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + res;
                    socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
                }
            } catch ( Exception ex ) {
                String httpResponse = "HTTP/1.1 500 Internal error\r\n\r\n"
                        + "UUUUPS: " + ex.getLocalizedMessage();
                socket.getOutputStream().write( httpResponse.getBytes( "UTF-8" ) );
            } finally {
                if ( socket != null ) {
                    socket.close();
                }
            }
        }
//        System.out.println( getFile("adding.html") );
    }

    private static String printAllParameters(HttpRequest req) {

        String s="";

        for ( Entry e : req.getParameters().entrySet() ) {
            s += "    " + e.getKey() + ": " + e.getValue() + "<br><br>";
        }


        String res= "";

        try {
             res = getResourceFileContents("allParam.tmpl");
        } catch (Exception e) {
            e.printStackTrace();
        }

        res = res.replace("$0",s);

        return res;
    }

    private static String mulOurNumbers(HttpRequest req) {


        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );

        return generateHTML("result.tmpl",first,second,String.valueOf(fi*si),"x");
    }

    private static String minusOurNumbers(HttpRequest req) {


        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );

        return generateHTML("result.tmpl",first,second,String.valueOf(fi-si),"x");
    }

    private static String divOurNumbers(HttpRequest req) {


        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );

        return generateHTML("result.tmpl",first,second,String.valueOf(fi/si),"x");
    }


    /*
    It is not part of the curriculum (pensum) to understand this method.
    You are more than welcome to bang your head on it though.
    */
    private static String getResourceFileContents( String fileName ) throws Exception {
        //Get file from resources folder
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        URL url = classLoader.getResource( fileName );
        File file = new File( url.getFile() );
        String content = new String( Files.readAllBytes( file.toPath() ) );
        return content;

    }

    private static  String generateHTML(String file, String a, String b, String c, String opr) {

        String res = null;
        try {
            res = getResourceFileContents("result.tmpl");
        } catch (Exception e) {
            e.printStackTrace();
        }
        res = res.replace( "$0", a);
        res = res.replace( "$1", b);
        res = res.replace( "$2", c );
        res = res.replace("$3",opr);
        return res;


    }


    private static String calOurNumbers( HttpRequest req ) {
        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );

        String operator = req.getParameter("operator");


        String resultat = "";

        switch (operator) {
            case "+":
                resultat = String.valueOf(fi+si);
                break;
            case "*":
                resultat = String.valueOf(fi*si);
                break;
            case "-":
                resultat = String.valueOf(fi-si);
                break;
            case "/":
                resultat = String.valueOf(fi/si);
                break;
            case "=":
                resultat = String.valueOf(fi == si);
                break;
            default:
                resultat = "666";
        }

        return generateHTML("result.tmpl", first , second, resultat, operator);

    }




    private static String addOurNumbers( HttpRequest req ) {
        String first = req.getParameter( "firstnumber" );
        String second = req.getParameter( "secondnumber" );
        int fi = Integer.parseInt( first );
        int si = Integer.parseInt( second );

        return generateHTML("result.tmpl",first,second,String.valueOf(fi+si),"+");
    }

    private static String RES = "<!DOCTYPE html>\n"
            + "<html lang=\"da\">\n"
            + "    <head>\n"
            + "        <title>Adding form</title>\n"
            + "        <meta charset=\"UTF-8\">\n"
            + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
            + "    </head>\n"
            + "    <body>\n"
            + "        <h1>Super: Resultatet af $0 + $1 blev: $2</h1>\n"
            + "        <a href=\"adding.html\">Læg to andre tal sammen</a>\n"
            + "    </body>\n"
            + "</html>\n";

}
