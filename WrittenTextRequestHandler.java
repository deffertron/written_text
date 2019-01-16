import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

import java.util.HashSet;
import java.util.Set;

public class WrittenTextRequestHandler extends SpeechletRequestStreamHandler
{
    private static final Set<String> supportedApplicationIds;

    static
    {
        supportedApplicationIds = new HashSet<>();
        supportedApplicationIds.add("amzn1.ask.skill.25d97b72-5833-467c-b2d8-0be862b08a93");
    }

    public WrittenTextRequestHandler()
    {
        super(new WrittenTextSpeechLet(), supportedApplicationIds);
    }
}
