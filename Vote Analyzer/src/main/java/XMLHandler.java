import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class XMLHandler extends DefaultHandler
{
    private Voter voter;
    private static SimpleDateFormat birthFormat = new SimpleDateFormat("yyyy.MM.dd");
    private HashMap<String,Short> voterCount;

    XMLHandler()
    {
        voterCount = new HashMap<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        try
        {
            if (qName.equals("voter") && voter == null)
            {
                Date birthDate = birthFormat.parse(attributes.getValue("birthDay"));
                voter = new Voter(attributes.getValue("name"), birthDate);
            }
            else if (qName.equals("visit") && voter != null)
            {
                int count = voterCount.getOrDefault(voter.toString(), (short)0);
                voterCount.put(voter.toString(), (short) (count + 1));
            }
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (!qName.equals("voter")) voter = null;
    }
}