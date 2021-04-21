import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Loader
{
    private static SimpleDateFormat birthFormat = new SimpleDateFormat("yyyy.MM.dd");
    private static SimpleDateFormat visitFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static HashMap<Integer, WorkTime> stationTime = new HashMap<>();
    private static HashMap<Voter, Integer> voterCount = new HashMap<>();

    public static void main(String[] args) throws Exception
    {
        String fileName = "res/data-18M.xml";

// DOM
        long start = System.currentTimeMillis();
        parseFileDom(fileName);
//        printVotingStationWorkTime();

        System.out.println("Used time on voting station work time\t" + printTime(System.currentTimeMillis() - start));
        long usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.printf("Used memory on voting station work time\t%,.2f Mb%n", (double) usage / Math.pow(2, 20));
        System.out.println("====================");

        start = System.currentTimeMillis();
//        printRepeatVoterDOM();

        System.out.println("Used time on repeat voters as DOM\t" + printTime(System.currentTimeMillis() - start));
        usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.printf("Used memory on repeat voters as DOM\t%,.2f Mb%n", (double) usage / Math.pow(2, 20));
        System.out.println("====================");

// SAXInsert
        start = System.currentTimeMillis();
        parseFileSAXInsert(fileName);
//        printRepeatVoterSAXInsert();

        System.out.println("Used time on repeat voters as SAXInsert\t\t" + printTime(System.currentTimeMillis() - start));
        usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.printf("Used memory on repeat voters as SAXInsert\t%,.2f Mb%n", (double) usage / Math.pow(2, 20));
        System.out.println("====================");

// SAXMultiInsert
        fileName = "res/data-1572M.xml";
        System.out.println("Repeat voters as SAXMultiInsert:");

        start = System.currentTimeMillis();
        Connection connection = DBConnection.getConnection();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandlerDB handler = new XMLHandlerDB();
        parser.parse(new File(fileName), handler);
        handler.executeInsert();
//        DBConnection.printRepeatVoterSAXMultiInsert();

        System.out.println("Used time on repeat voters as SAXMultiInsert\t\t" + printTime(System.currentTimeMillis() - start));
        usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.printf("Used memory on repeat voters as SAXMultiInsert\t%,.2f Mb%n", (double) usage / Math.pow(2, 20));
        System.out.println("====================");

        connection.close();
    }

    private static void parseFileSAXInsert(String fileName) throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLHandler handler = new XMLHandler();
        parser.parse(new File(fileName), handler);
    }

    private static void parseFileDom(String fileName) throws Exception
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(fileName));

        findEqualVoters(doc);
        fixWorkTimes(doc);
    }

    private static void findEqualVoters(Document doc) throws Exception
    {
        NodeList voters = doc.getElementsByTagName("voter");
        int votersCount = voters.getLength();

        for (int i = 0; i < votersCount; i++)
        {
            Node node = voters.item(i);
            NamedNodeMap attributes = node.getAttributes();

            String name = attributes.getNamedItem("name").getNodeValue();
            Date birthDate = birthFormat.parse(attributes.getNamedItem("birthDay").getNodeValue());

            Voter voter = new Voter(name, birthDate);
            Integer count = voterCount.get(voter);
            voterCount.put(voter, count == null ? 1 : count + 1);
        }
    }

    private static void fixWorkTimes(Document doc) throws Exception
    {
        NodeList visits = doc.getElementsByTagName("visit");
        int visitCount = visits.getLength();

        for (int i = 0; i < visitCount; i++)
        {
            Node node = visits.item(i);
            NamedNodeMap attributes = node.getAttributes();

            Integer station = Integer.parseInt(attributes.getNamedItem("station").getNodeValue());
            Date time = visitFormat.parse(attributes.getNamedItem("time").getNodeValue());
            WorkTime workTime = stationTime.get(station);
            if (workTime == null)
            {
                workTime = new WorkTime();
                stationTime.put(station, workTime);
            }
            workTime.addVisitTime(time.getTime());
        }
    }

    private static void printVotingStationWorkTime()
    {
        System.out.println("Voting station work time:");
        for (Integer votingStation : stationTime.keySet())
        {
            WorkTime workTime = stationTime.get(votingStation);
            System.out.println("\t" + votingStation + " - " + workTime);
        }
    }

    public static void printRepeatVoterDOM()
    {
        System.out.println("Repeat voters as DOM:");
        for (Voter voter : voterCount.keySet())
        {
            Integer count = voterCount.get(voter);
            if (count > 1)
            {
                System.out.println("\t" + voter + " - " + count);
            }
        }
    }

    public static void printRepeatVoterSAXInsert()
    {
        System.out.println("Repeat voters as SAXInsert:");
        for (Voter voter : voterCount.keySet())
        {
            int count = voterCount.get(voter);
            if (count > 1)
            {
                System.out.println("\t" + voter + " - " + count);
            }
        }
    }

    public static String printTime(long milliseconds)
    {
        long min = (milliseconds / 1000) / 60;
        long sec = (milliseconds / 1000) % 60;
        long ms = (milliseconds % 1000);

        String printTime = String.format("%d minutes and %d seconds and %d milliseconds", min, sec, ms);
        return printTime;
    }
}