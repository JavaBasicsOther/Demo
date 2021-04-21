import java.text.SimpleDateFormat;
import java.util.Date;

public class Voter
{
    private String name;
    private Date birthDate;

    public Voter(String name, Date birthDay)
    {
        this.name = name;
        this.birthDate = birthDay;
    }

    @Override
    public boolean equals(Object obj)
    {
        Voter voter = (Voter) obj;
        return name.equals(voter.name) && birthDate.equals(voter.birthDate);
    }

    @Override
    public int hashCode()
    {
        long code = name.hashCode() + birthDate.hashCode();

        while(code > Integer.MAX_VALUE)
        {
            code = code/10;
        }
        return (int) code;
    }

    public String toString()
    {
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy.MM.dd");
        return name + " (" + dayFormat.format(birthDate) + ")";
    }

    public String getName()
    {
        return name;
    }

    public Date getbirthDate()
    {
        return birthDate;
    }
}