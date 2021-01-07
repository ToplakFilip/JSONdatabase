package client;
import com.beust.jcommander.Parameter;

public class Args {

    @Parameter(names = {"-t"}, description = "[get, set, delete, exit] requests")
    public String request;

    /*
    @Parameter(names = {"-k"}, description = "key")
    public List<String> key = new ArrayList<>();
     */
    @Parameter(names = {"-k"}, description = "key")
    public String key;

    @Parameter(names = {"-v"}, description = "set new value, only for [set] request")
    public String value;

    @Parameter(names = {"-in"}, description = "Read inside .json files")
    public String jsonFileName;

}