import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XMLHandlerDB extends DefaultHandler
{
    private Voter voter;
    private static final SimpleDateFormat birthFormat = new SimpleDateFormat("yyyy.MM.dd");
    private final Connection connection = DBConnection.getConnection();
    private StringBuilder insertQuery = new StringBuilder();
    private List<StringBuilder> queries = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
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
                String date = birthFormat.format(voter.getbirthDate());
                insertToStringBuilder(insertQuery, date);
            }
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }
    }

    public void executeInsert() throws SQLException
    {
        queries.add(insertQuery);
        insertQuery = null;

        for (StringBuilder query : queries)
        {
            String sql = "insert into voter_count (name, birthDate, count) values "+ query.toString() + " on duplicate key update count = count + 1";
            connection.createStatement().execute(sql);
        }
    }

    private void insertToStringBuilder(StringBuilder builder, String date)
    {
        builder.append(builder.length() == 0 ? "" : ",")
                .append("('")
                .append(voter.getName())
                .append("', '")
                .append(date)
                .append("', 1)");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals("voter"))
        {
            if (insertQuery.length() > 500000)
            {
                queries.add(insertQuery);
                insertQuery = new StringBuilder();
            }
            voter = null;
        }
    }
}