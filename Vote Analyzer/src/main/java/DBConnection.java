import java.sql.*;

public class DBConnection
{
    private static Connection connection;
    private static final String URL_START = "jdbc:mysql://localhost:3306/voteanalyzer";
    private static final String URL_END = "&useSSL=true&serverTimezone=UTC";
    private static final String USER_NAME = "skillbox";
    private static final String PASSWORD = "";


    public static Connection getConnection()
    {
        if (connection == null)
        {
            try
            {
                connection = DriverManager.getConnection(URL_START + "?user=" + USER_NAME + "&password=" + PASSWORD + URL_END);
                connection.createStatement().execute(
                "drop table if exists voteanalyzer.voter_count");
                connection.createStatement().execute(
                "create table voteanalyzer.voter_count (" +
                    "id INT not null auto_increment, " +
                    "name TINYTEXT not null, " +
                    "birthDate DATE not null, " +
                    "count INT not null, " +
                    "primary key (ID))");
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        return connection;
    }

    public static void printRepeatVoterSAXMultiInsert()
    {
        try
        {
            connection.createStatement().execute(
            "drop table if exists voteanalyzer.voter_repeat");
            connection.createStatement().execute(
            "create table voteanalyzer.voter_repeat (name TINYTEXT, count INT, birthDate DATE)");
            connection.createStatement().execute(
            "insert into voteanalyzer.voter_repeat (name, birthDate, count)" +
                "select name, birthDate, count(*) from voteanalyzer.voter_count group by name, birthDate having count(*) > 1 order by count(*) desc");

            ResultSet resultSet = connection.createStatement().executeQuery(
            "select name, date_format(birthDate, '%Y.%m.%d') birthDate, count from voteanalyzer.voter_repeat");

            while (resultSet.next())
            {
                System.out.println("\t" +
                    resultSet.getString("name") + " (" +
                    resultSet.getString("birthDate") + ") - " +
                    resultSet.getInt("count"));
            }
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
        }
    }
}