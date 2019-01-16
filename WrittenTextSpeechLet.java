import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;

import com.amazon.speech.speechlet.interfaces.system.System;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WrittenTextSpeechLet implements SpeechletV2
{
    private Logger logger = LogManager.getLogger(WrittenTextSpeechLet.class);

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope)
    {
        logger.debug("Session started at : " + speechletRequestEnvelope.getRequest().getTimestamp());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope)
    {
        Session session = speechletRequestEnvelope.getSession();

        String welcomeMessage = "Hi, welcome to written text. " +
                "My work is extract the handwritten text and also the typed text from that image file. " +
                "Ok, this is a file type task, that means we want some storage place to store and retrieve the image files called s3 storage. " +
                "I hope you already know the bucket name,file name and file format. " +
                "Or, if you don't know. " +
                "Don't worry simply say ' storage helper ' to how to get the bucket name,file name and file format. " +
                "And then another important thing i support only jpg and png file formats. " +
                "So, please choose jpg and png images only from your storage. " +
                "Now, you know how to say the bucket name,file name and file format to me, simply say the bucket name with the keyword bucket name. " +
                "Example, bucket name my bucket. " +
                "Otherwise if you don't how to say the bucket name,file name and file format to me. " +
                "Don't worry simply say ' file helper '. " +
                "Okay, now say the bucket name with keyword bucket name. ";

        String cardTitle = "Welcome Message Card";

        String welcomeRePromptMessage = "Now, you know how to say the bucket name,file name and file format to me, simply say the bucket name with the keyword bucket name. " +
                "Example, bucket name my bucket. " +
                "Otherwise if you don't how to say the bucket name,file name and file format to me. " +
                "Don't worry simply say ' file helper '. " +
                "Okay, now say the bucket name with keyword bucket name. ";

        logger.debug(cardTitle + " : " + welcomeMessage);

        session.setAttribute("repeat_message",welcomeMessage);
        session.setAttribute("repeat_re_prompt_message",welcomeRePromptMessage);

        return getMessageWithSimpleCardResponse(welcomeMessage,cardTitle,welcomeRePromptMessage,true);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope)
    {
        Intent intent = speechletRequestEnvelope.getRequest().getIntent();

        Session session = speechletRequestEnvelope.getSession();

        if (intent != null)
        {
            String intentName = intent.getName();

            switch (intentName) {
                case "StorageHelperIntent": {
                    String storageHelperMessage = "Okay, s3 storage is a cloud storage of amazon aws. " +
                            "Okay, i say a steps to how to store your files in amazon aws s3 storage. " +
                            "First step, go to amazon aws and create your account. " +
                            "And then second step is, type s3 in the search box and choose s3 (scalable storage in the cloud). " +
                            "And then third step is, click create bucket and type your bucket name and choose a region in the name and region tab." +
                            "And then click next and in the configure options tab uncheck all checked options. " +
                            "And click next and set manage system permissions as 'grant amazon s3 log delivery group write access to this bucket' in the set permissions tab. " +
                            "And click next and click create bucket. " +
                            "Now your bucket has been created. " +
                            "Click on your bucket name and click upload, choose your file from your laptop or pc. " +
                            "And set manage public permission as 'grant public read access to this object(s)' in the set permissions tab. " +
                            "And click next and again click next and click upload. " +
                            "Once your file is uploaded get the url of the storage path by clicking on the file name. " +
                            "Once you got bucket name,file name and file format come back and say it to me. " +
                            "If you don't know, how to say a bucket name,file name and file format to me. " +
                            "Simply, say ' file helper '. " +
                            "Otherwise, say the bucket name with keyword bucket name. ";

                    String cardTitle = "Storage Helper Card";

                    String storageHelperRePromptMessage = "Otherwise, say the bucket name with keyword bucket name. ";

                    logger.debug(cardTitle + " : " + storageHelperMessage);

                    session.setAttribute("repeat_message", storageHelperMessage);
                    session.setAttribute("repeat_re_prompt_message", storageHelperRePromptMessage);

                    return getMessageWithSimpleCardResponse(storageHelperMessage, cardTitle, storageHelperRePromptMessage, true);
                }
                case "FileHelperIntent": {
                    String filePathMessage = "Okay, the file path url of your file will be like ' https://s3.amazonaws.com/imagefiber/L1.PNG '. " +
                            "Here 'https://s3.amazonaws.com' is a host name of the web page. " +
                            "You don't say the host name, because i can automatically recognize the host name. " +
                            "Only say the bucket name,file name and file format without the hostname and the forward slashes. " +
                            "For example, i ask : say the bucket name. " +
                            "Now you say the bucket name with the keyword bucket name. " +
                            "Example \"bucket name 'name of your bucket'\", here bucket name is imagefiber. " +
                            "And i ask : say the file name.  " +
                            "Now you say the file name with the keyword file name. " +
                            "Example \"filename 'name of your file name'\", here file name is L1. " +
                            "And then finally i ask the file format. " +
                            "Now you say the format of your file with the keyword file format. " +
                            "Example \"file format 'format of your file'\", here format is PNG. " +
                            "I hope you understand the instructions. " +
                            "And also another thing, here is supports fue file formats only. " +
                            "That file formats are png and jpg or jpeg in image files. " +
                            "Now you ready to say the file path url, simply say the bucket name with the keyword bucket name";

                    String cardTitle = "File Helper Message";

                    String filePathRePromptMessage = "Now you ready to say the file path url, simply say the bucket name with the keyword bucket name";

                    logger.debug(cardTitle + " : " + filePathMessage);

                    session.setAttribute("repeat_message", filePathMessage);
                    session.setAttribute("repeat_re_prompt_message", filePathRePromptMessage);

                    return getMessageWithSimpleCardResponse(filePathMessage, cardTitle, filePathRePromptMessage, true);
                }
                case "GetBucketNameIntent": {
                    String bucketName = intent.getSlot("bucket_name").getValue().toLowerCase();

                    if (bucketName.isEmpty()) {
                        return getBucketNameRecognizeEmptyResponse(session);
                    } else {
                        String getBucketNameMessage = "Confirm " + bucketName + " is your bucket name. " +
                                "If yes, say yes this is my bucket name. " +
                                "If no, say no this is not my bucket name. ";

                        String cardTitle = "Confirm Bucket Name Message";

                        logger.debug(cardTitle + " : " + getBucketNameMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("repeat_message", getBucketNameMessage);
                        session.setAttribute("repeat_re_prompt_message", getBucketNameMessage);

                        return getMessageWithSimpleCardResponse(getBucketNameMessage, cardTitle, getBucketNameMessage, true);
                    }
                }
                case "ThisIsMyBucketNameIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else {
                        String thisIsMyBucketNameMessage = "Okay now say the file name of your file with the keyword file name. " +
                                "Example, file name orange. ";

                        String cardTitle = "This Is My Bucket Name Message";

                        logger.debug(cardTitle + " : " + thisIsMyBucketNameMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("repeat_message", thisIsMyBucketNameMessage);
                        session.setAttribute("repeat_re_prompt_message", thisIsMyBucketNameMessage);

                        return getMessageWithSimpleCardResponse(thisIsMyBucketNameMessage, cardTitle, thisIsMyBucketNameMessage, true);
                    }
                }
                case "ThisIsNotMyBucketNameIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else {
                        String thisIsNotMyBucketNameMessage = "Okay don't worry please say the bucket name again with the keyword bucket name. " +
                                "Example, bucket name orange. ";

                        String cardTitle = "This Is Not My Bucket Name Message";

                        logger.debug(cardTitle + " : " + thisIsNotMyBucketNameMessage);

                        session.setAttribute("repeat_message", thisIsNotMyBucketNameMessage);
                        session.setAttribute("repeat_re_prompt_message", thisIsNotMyBucketNameMessage);

                        return getMessageWithSimpleCardResponse(thisIsNotMyBucketNameMessage, cardTitle, thisIsNotMyBucketNameMessage, true);
                    }
                }
                case "GetFileNameIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = intent.getSlot("file_name").getValue().toLowerCase();

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameRecognizeEmptyResponse(bucketName, session);
                    } else {
                        String getFileNameMessage = "Confirm " + fileName + " is your file name. " +
                                "If yes, say yes this is my file name. " +
                                "If no, say no this is not my file name. ";

                        String cardTitle = "Confirm File Name Message";

                        logger.debug(cardTitle + " : " + getFileNameMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("file_name", fileName);
                        session.setAttribute("repeat_message", getFileNameMessage);
                        session.setAttribute("repeat_re_prompt_message", getFileNameMessage);

                        return getMessageWithSimpleCardResponse(getFileNameMessage, cardTitle, getFileNameMessage, true);
                    }
                }
                case "ThisIsMyFileNameIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = getStoredSessionFileName(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameEmptyResponse(bucketName, session);
                    } else {
                        String thisIsMyFileNameMessage = "Okay, now say your file format with the keyword file format. " +
                                "Example, file format jpg. ";

                        String cardTitle = "This Is My File Name Message";

                        logger.debug(cardTitle + " : " + thisIsMyFileNameMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("file_name", fileName);
                        session.setAttribute("repeat_message", thisIsMyFileNameMessage);
                        session.setAttribute("repeat_re_prompt_message", thisIsMyFileNameMessage);

                        return getMessageWithSimpleCardResponse(thisIsMyFileNameMessage, cardTitle, thisIsMyFileNameMessage, true);
                    }
                }
                case "ThisIsNotMyFileNameIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = getStoredSessionFileName(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameEmptyResponse(bucketName, session);
                    } else {
                        String thisIsNotMyFileNameMessage = "Okay don't worry please say the file name again with the keyword file name. " +
                                "Example, file name apple. ";

                        String cardTitle = "This Is Not My File Name Message";

                        logger.debug(cardTitle + " : " + thisIsNotMyFileNameMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("repeat_message", thisIsNotMyFileNameMessage);
                        session.setAttribute("repeat_re_prompt_message", thisIsNotMyFileNameMessage);

                        return getMessageWithSimpleCardResponse(thisIsNotMyFileNameMessage, cardTitle, thisIsNotMyFileNameMessage, true);
                    }
                }
                case "GetFileFormatIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = getStoredSessionFileName(session);

                    String fileFormat = intent.getSlot("file_format").getValue().toLowerCase();

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameEmptyResponse(bucketName, session);
                    } else if (fileFormat.isEmpty()) {
                        return getFileFormatRecognizeEmptyResponse(bucketName, fileName, session);
                    } else {
                        String getFileFormatMessage = "Confirm " + fileFormat + " is your file format. " +
                                "If yes, say yes this is my file format. " +
                                "If no, say no this is not my file format. ";

                        String cardTitle = "Confirm File Format Message";

                        logger.debug(cardTitle + " : " + getFileFormatMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("file_name", fileName);
                        session.setAttribute("file_format", fileFormat);
                        session.setAttribute("repeat_message", getFileFormatMessage);
                        session.setAttribute("repeat_re_prompt_message", getFileFormatMessage);

                        return getMessageWithSimpleCardResponse(getFileFormatMessage, cardTitle, getFileFormatMessage, true);
                    }
                }
                case "ThisIsMyFileFormatIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = getStoredSessionFileName(session);

                    String fileFormat = getStoredSessionFileFormat(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameEmptyResponse(bucketName, session);
                    } else if (fileFormat.isEmpty()) {
                        return getFileFormatEmptyResponse(bucketName, fileName, session);
                    } else {
                        String hostName = "https://s3.amazonaws.com/";

                        String fileUrl = hostName + bucketName + "/" + fileName + "." + fileFormat;

                        String thisIsMyFileFormatMessage = "Okay, now confirm this " + fileUrl + " is your file url. " +
                                "If yes, say yes this is my file url. " +
                                "Or if no, say no this is not my file url. ";

                        String cardTitle = "This Is My File Format Message";

                        logger.debug(cardTitle + " : " + thisIsMyFileFormatMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("file_name", fileName);
                        session.setAttribute("file_format", fileFormat);
                        session.setAttribute("repeat_message", thisIsMyFileFormatMessage);
                        session.setAttribute("repeat_re_prompt_message", thisIsMyFileFormatMessage);

                        return getMessageWithSimpleCardResponse(thisIsMyFileFormatMessage, cardTitle, thisIsMyFileFormatMessage, true);
                    }
                }
                case "ThisIsNotMyFileFormatIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = getStoredSessionFileName(session);

                    String fileFormat = getStoredSessionFileFormat(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameEmptyResponse(bucketName, session);
                    } else if (fileFormat.isEmpty()) {
                        return getFileFormatEmptyResponse(bucketName, fileName, session);
                    } else {
                        String thisIsNotMyFileFormatMessage = "Okay don't worry please say the file format again with the keyword file format. Example, file format jpg. ";

                        String cardTitle = "This Is Not My File Format Message";

                        logger.debug(cardTitle + " : " + thisIsNotMyFileFormatMessage);

                        session.setAttribute("bucket_name", bucketName);
                        session.setAttribute("file_name", fileName);
                        session.setAttribute("repeat_message", thisIsNotMyFileFormatMessage);
                        session.setAttribute("repeat_re_prompt_message", thisIsNotMyFileFormatMessage);

                        return getMessageWithSimpleCardResponse(thisIsNotMyFileFormatMessage, cardTitle, thisIsNotMyFileFormatMessage, true);
                    }
                }
                case "ThisIsMyFileUrlIntent": {
                    String bucketName = getStoredSessionBucketName(session);

                    String fileName = getStoredSessionFileName(session);

                    String fileFormat = getStoredSessionFileFormat(session);

                    if (bucketName.isEmpty()) {
                        return getBucketNameEmptyResponse(session);
                    } else if (fileName.isEmpty()) {
                        return getFileNameEmptyResponse(bucketName, session);
                    } else if (fileFormat.isEmpty()) {
                        return getFileFormatEmptyResponse(bucketName, fileName, session);
                    } else {
                        try {
                            String imageUrl = "https://s3.amazonaws.com/" + bucketName + "/" + fileName + "." + fileFormat;

                            URL url = new URL(imageUrl);

                            InputStream in = new BufferedInputStream(url.openStream());

                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                            byte[] buf = new byte[1024];

                            int n;

                            while (-1 != (n = in.read(buf))) {
                                byteArrayOutputStream.write(buf, 0, n);
                            }

                            byteArrayOutputStream.close();
                            in.close();

                            byte[] responseBytes = byteArrayOutputStream.toByteArray();

                            List<AnnotateImageRequest> requests = new ArrayList<>();

                            ByteString imgBytes = ByteString.copyFrom(responseBytes);

                            Image img = Image.newBuilder().setContent(imgBytes).build();
                            Feature feat = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
                            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                            requests.add(request);

                            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                                BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                                List<AnnotateImageResponse> responses = response.getResponsesList();
                                client.close();

                                StringBuilder stringBuilder = new StringBuilder();

                                for (AnnotateImageResponse res : responses) {
                                    if (res.hasError()) {
                                        return getImageTaskFailureResponse(session);
                                    } else {
                                        TextAnnotation annotation = res.getFullTextAnnotation();

                                        stringBuilder.append(annotation.getText());
                                    }
                                }

                                String imageTaskSuccessMessage = "Okay, now i say the extracted text from the image file. " +
                                        stringBuilder.toString() +
                                        ". Okay, this are the detected texts from your image file. " +
                                        "Okay, if you want to perform another handwritten text task on image file. " +
                                        "Simply, say the bucket name with the keyword bucket name. ";

                                String cardTitle = "Image Task Success Message";

                                String imageTaskSuccessRePromptMessage = "Simply, say the bucket name with the keyword bucket name. ";

                                logger.debug(cardTitle + " : " + imageTaskSuccessMessage);

                                session.removeAttribute("bucket_name");
                                session.removeAttribute("file_name");
                                session.removeAttribute("file_format");
                                session.setAttribute("repeat_message", imageTaskSuccessMessage);
                                session.setAttribute("repeat_re_prompt_message", imageTaskSuccessRePromptMessage);

                                return getMessageWithSimpleCardResponse(imageTaskSuccessMessage, cardTitle, imageTaskSuccessRePromptMessage, true);
                            }
                        } catch (IOException e)
                        {
                            logger.debug("error " + " : " + e.getMessage());

                            return getImageTaskFailureResponse(session);
                        }
                    }
                }
                case "ThisIsNotMyFileUrlIntent": {
                    String thisIsNotMyFileUrlMessage = "Okay, don't worry please say the bucket name,file name and file format again one by one. " +
                            "Now first say the bucket name with the keyword bucket name. ";

                    String cardTitle = "This Is Not My File Url Message";

                    String thisIsNotMyFileUrlRePromptMessage = "Now first say the bucket name with the keyword bucket name. ";

                    logger.debug(cardTitle + " : " + thisIsNotMyFileUrlMessage);

                    session.setAttribute("repeat_message", thisIsNotMyFileUrlMessage);
                    session.setAttribute("repeat_re_prompt_message", thisIsNotMyFileUrlRePromptMessage);

                    return getMessageWithSimpleCardResponse(thisIsNotMyFileUrlMessage, cardTitle, thisIsNotMyFileUrlRePromptMessage, true);
                }
                case "AMAZON.HelpIntent": {
                    String helpMessage = "Hi, It's a pleasure to help to you. " +
                            "My work is extract the handwritten text and also the typed text from that image file. " +
                            "Ok, this is a file type task, that means we want some storage place to store and retrieve the image files called s3 storage. " +
                            "I hope you already know the bucket name,file name and file format. " +
                            "Or, if you don't know. " +
                            "Don't worry simply say ' storage helper ' to how to get the bucket name,file name and file format. " +
                            "And then another important thing i support only jpg and png file formats. " +
                            "So, please choose jpg and png images only from your storage. " +
                            "Now, you know how to say the bucket name,file name and file format to me, simply say the bucket name with the keyword bucket name. " +
                            "Example, bucket name my bucket. " +
                            "Otherwise if you don't how to say the bucket name,file name and file format to me. " +
                            "Don't worry simply say ' file helper '. " +
                            "Okay, now say the bucket name with keyword bucket name. ";

                    String cardTitle = "Help Message";

                    String helpRePromptMessage = "Now, choose one task name from above list. " +
                            "Okay, now say the bucket name with keyword bucket name. ";

                    logger.debug(cardTitle + " : " + helpMessage);

                    session.setAttribute("repeat_message", helpMessage);
                    session.setAttribute("repeat_re_prompt_message", helpRePromptMessage);

                    return getMessageWithSimpleCardResponse(helpMessage, cardTitle, helpRePromptMessage, true);
                }
                case "AMAZON.RepeatIntent": {
                    String repeatMessage = intent.getSlot("repeat_message").getValue();

                    String repeatRePromptMessage = intent.getSlot("repeat_re_prompt_message").getValue();

                    String cardTitle = "Repeat Message";

                    logger.debug(cardTitle + " : " + repeatMessage);

                    session.setAttribute("repeat_message", repeatMessage);
                    session.setAttribute("repeat_re_prompt_message", repeatRePromptMessage);

                    return getMessageWithSimpleCardResponse(repeatMessage, cardTitle, repeatRePromptMessage, true);
                }
                case "AMAZON.FallbackIntent":
                    return getFallbackResponse(session);
                case "AMAZON.StopIntent":
                    return getStopOrCancelResponse(session);
                case "AMAZON.CancelIntent":
                    return getStopOrCancelResponse(session);
                case "AMAZON.YesIntent":
                    return getYesResponse(session);
                case "AMAZON.NoIntent":
                    return getNoResponse(session);
                default:
                    return getFallbackResponse(session);
            }
        }
        else
        {
            return getFallbackResponse(session);
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope)
    {

    }

    //Get Session Attributes

    private String getStoredSessionBucketName(Session session)
    {
        String filePath = (String) session.getAttribute("bucket_name");

        if (filePath != null)
        {
            return filePath;
        }
        else
        {
            return "";
        }
    }

    private String getStoredSessionFileName(Session session)
    {
        String fileName = (String) session.getAttribute("file_name");

        if (fileName != null)
        {
            return fileName;
        }
        else
        {
            return "";
        }
    }

    private String getStoredSessionFileFormat(Session session)
    {
        String fileFormat = (String) session.getAttribute("file_format");

        if (fileFormat != null)
        {
            return fileFormat;
        }
        else
        {
            return "";
        }
    }

    //Intent Response

    private SpeechletResponse getBucketNameRecognizeEmptyResponse(Session session)
    {
        String bucketNameRecognizeEmptyMessage = "Sorry, i could not find your bucket name. " +
                "So please, first say the bucket name with the keyword bucket name. ";

        String cardTitle = "Bucket Name Recognize Empty Message";

        String bucketNameRecognizeEmptyRePromptMessage = "So please, first say the bucket name with the keyword bucket name. ";

        logger.debug(cardTitle + " : " + bucketNameRecognizeEmptyMessage);

        session.setAttribute("repeat_message",bucketNameRecognizeEmptyMessage);
        session.setAttribute("repeat_re_prompt_message",bucketNameRecognizeEmptyRePromptMessage);

        return getMessageWithSimpleCardResponse(bucketNameRecognizeEmptyMessage,cardTitle,bucketNameRecognizeEmptyRePromptMessage,true);
    }

    private SpeechletResponse getBucketNameEmptyResponse(Session session)
    {
        String bucketNameEmptyMessage = "Sorry, i could not find your bucket name. " +
                "So please, first say the bucket name with the keyword bucket name. ";

        String cardTitle = "Get Bucket Name Empty Message";

        String bucketNameEmptyRePromptMessage = "So please, first say the bucket name with the keyword bucket name. ";

        logger.debug(cardTitle + " : " + bucketNameEmptyMessage);

        session.setAttribute("repeat_message",bucketNameEmptyMessage);
        session.setAttribute("repeat_re_prompt_message",bucketNameEmptyRePromptMessage);

        return getMessageWithSimpleCardResponse(bucketNameEmptyMessage,cardTitle,bucketNameEmptyRePromptMessage,true);
    }

    private SpeechletResponse getFileNameRecognizeEmptyResponse(String bucketName, Session session)
    {
        String bucketNameRecognizeEmptyMessage = "Sorry, i could not understand your voice. " +
                "Please say the file name again with the keyword file name. ";

        String cardTitle = "File Name Recognize Empty Message";

        String bucketNameRecognizeEmptyRePromptMessage = "Please say the file name again with keyword file name. ";

        logger.debug(cardTitle + " : " + bucketNameRecognizeEmptyMessage);

        session.setAttribute("bucket_name",bucketName);
        session.setAttribute("repeat_message",bucketNameRecognizeEmptyMessage);
        session.setAttribute("repeat_re_prompt_message",bucketNameRecognizeEmptyRePromptMessage);

        return getMessageWithSimpleCardResponse(bucketNameRecognizeEmptyMessage,cardTitle,bucketNameRecognizeEmptyRePromptMessage,true);
    }

    private SpeechletResponse getFileNameEmptyResponse(String bucketName, Session session)
    {
        String bucketNameEmptyMessage = "Sorry, i could not find your file name. " +
                "So please, first say the file name with the keyword file name. ";

        String cardTitle = "File Name Empty Message";

        String bucketNameEmptyRePromptMessage = "So please, first say the file name with the keyword file name. ";

        logger.debug(cardTitle + " : " + bucketNameEmptyMessage);

        session.setAttribute("bucket_name",bucketName);
        session.setAttribute("repeat_message",bucketNameEmptyMessage);
        session.setAttribute("repeat_re_prompt_message",bucketNameEmptyRePromptMessage);

        return getMessageWithSimpleCardResponse(bucketNameEmptyMessage,cardTitle,bucketNameEmptyRePromptMessage,true);
    }

    private SpeechletResponse getFileFormatRecognizeEmptyResponse(String bucketName, String fileName, Session session)
    {
        String fileFormatRecognizeEmptyMessage = "Sorry, i could not understand your voice. " +
                "Please say the file format again with the keyword file format. ";

        String cardTitle = "File Format Recognize Empty Message";

        String fileFormatRecognizeEmptyRePromptMessage = "Please say the file format again with keyword file format. ";

        logger.debug(cardTitle + " : " + fileFormatRecognizeEmptyMessage);

        session.setAttribute("bucket_name",bucketName);
        session.setAttribute("file_name",fileName);
        session.setAttribute("repeat_message",fileFormatRecognizeEmptyMessage);
        session.setAttribute("repeat_re_prompt_message",fileFormatRecognizeEmptyRePromptMessage);

        return getMessageWithSimpleCardResponse(fileFormatRecognizeEmptyMessage,cardTitle,fileFormatRecognizeEmptyRePromptMessage,true);
    }

    private SpeechletResponse getFileFormatEmptyResponse(String bucketName, String fileName, Session session)
    {
        String fileFormatEmptyMessage = "Sorry, i could not find your file format. " +
                "So please, first say the file format with the keyword file format. ";

        String cardTitle = "File Format Empty Message";

        String fileFormatEmptyRePromptMessage = "So please, first say the file format with the keyword file format. ";

        logger.debug(cardTitle + " : " + fileFormatEmptyMessage);

        session.setAttribute("bucket_name",bucketName);
        session.setAttribute("file_name",fileName);
        session.setAttribute("repeat_message",fileFormatEmptyMessage);
        session.setAttribute("repeat_re_prompt_message",fileFormatEmptyRePromptMessage);

        return getMessageWithSimpleCardResponse(fileFormatEmptyMessage,cardTitle,fileFormatEmptyRePromptMessage,true);
    }

    private SpeechletResponse getImageTaskFailureResponse(Session session)
    {
        String imageModerationTaskErrorMessage = "Unfortunately, i could not perform this image task now. " +
                "Because i could not find your image or may be some error has been occurred. " +
                "And also the task has been terminated. " +
                "Sorry for that. " +
                "Okay, please say a another bucket name to start a new task with the keyword bucket name. ";

        String cardTitle = "Image Task Failure Message";

        String imageModerationTaskErrorRePromptMessage = "Okay, please say a another bucket name to start a new task with the keyword bucket name. ";

        logger.debug(cardTitle + " : " + imageModerationTaskErrorMessage);

        session.removeAttribute("bucket_name");
        session.removeAttribute("file_name");
        session.removeAttribute("file_format");
        session.setAttribute("repeat_message", imageModerationTaskErrorMessage);
        session.setAttribute("repeat_re_prompt_message", imageModerationTaskErrorRePromptMessage);

        return getMessageWithSimpleCardResponse(imageModerationTaskErrorMessage, cardTitle, imageModerationTaskErrorRePromptMessage, true);
    }

    private SpeechletResponse getFallbackResponse(Session session)
    {
        if (!getStoredSessionBucketName(session).isEmpty())
        {
            session.removeAttribute("bucket_name");
        }

        if (!getStoredSessionFileName(session).isEmpty())
        {
            session.removeAttribute("file_name");
        }

        if (!getStoredSessionFileFormat(session).isEmpty())
        {
            session.removeAttribute("file_format");
        }

        String fallbackMessage = "Sorry, some error has been occurred or some internal problem occurred. " +
                "Or, i could not understand your words. " +
                "So, please start your task freshly. " +
                "Or, if you want help say help. " +
                "Okay, now say a bucket name with the keyword bucket name. ";

        String cardTitle = "Fallback Message";

        String fallbackRePromptMessage = "Okay, now say a bucket name with the keyword bucket name. ";

        logger.debug(cardTitle + " : " + fallbackMessage);

        session.setAttribute("repeat_message",fallbackMessage);
        session.setAttribute("repeat_re_prompt_message",fallbackRePromptMessage);

        return getMessageWithSimpleCardResponse(fallbackMessage,cardTitle,fallbackRePromptMessage,true);
    }

    private SpeechletResponse getStopOrCancelResponse(Session session)
    {
        if (!getStoredSessionBucketName(session).isEmpty())
        {
            session.removeAttribute("bucket_name");
        }

        if (!getStoredSessionFileName(session).isEmpty())
        {
            session.removeAttribute("file_name");
        }

        if (!getStoredSessionFileFormat(session).isEmpty())
        {
            session.removeAttribute("file_format");
        }

        String stopOrCancelMessage = "Would you like to cancel or stop all the tasks and conversations?. " +
                "If yes say yes. " +
                "If no say no. ";

        String cardTitle = "Stop or Cancel Message";

        logger.debug(cardTitle + " : " + stopOrCancelMessage);

        session.setAttribute("repeat_message",stopOrCancelMessage);
        session.setAttribute("repeat_re_prompt_message",stopOrCancelMessage);

        return getMessageWithSimpleCardResponse(stopOrCancelMessage,cardTitle,stopOrCancelMessage,true);
    }

    private SpeechletResponse getYesResponse(Session session)
    {
        String yesMessage = "Ok, i stopped and terminated all the tasks and conversations. If you like to speak to me again. Simply you can say alexa, open written text.";

        String cardTitle = "Yes Message";

        logger.debug(cardTitle + " : " + yesMessage);

        session.setAttribute("repeat_message",yesMessage);
        session.setAttribute("repeat_re_prompt_message",yesMessage);

        return getMessageWithSimpleCardResponse(yesMessage,cardTitle,yesMessage,false);
    }

    private SpeechletResponse getNoResponse(Session session)
    {
        if (!getStoredSessionBucketName(session).isEmpty())
        {
            session.removeAttribute("bucket_name");
        }

        if (!getStoredSessionFileName(session).isEmpty())
        {
            session.removeAttribute("file_name");
        }

        if (!getStoredSessionFileFormat(session).isEmpty())
        {
            session.removeAttribute("file_format");
        }

        String noMessage = "Ok, don't worry. We can continue the conversation. " +
                "The all tasks has been terminated. " +
                "So, please say the bucket name with keyword bukcet name to start a new task. ";

        String cardTitle = "No Message";

        String noRePromptMessage = "So, please say the bucket name with keyword bukcet name to start a new task. ";

        logger.debug(cardTitle + " : " + noMessage);

        session.setAttribute("repeat_message",noMessage);
        session.setAttribute("repeat_re_prompt_message",noRePromptMessage);

        return getMessageWithSimpleCardResponse(noMessage,cardTitle,noRePromptMessage,true);
    }

    // Speechlet Simple Card Response

    private static SpeechletResponse getMessageWithSimpleCardResponse(String message, String cardTitle, String rePromptMessage, boolean askResponse)
    {
        SimpleCard simpleCard = new SimpleCard();
        simpleCard.setTitle(cardTitle);
        simpleCard.setContent(message);

        PlainTextOutputSpeech plainTextOutputSpeech = new PlainTextOutputSpeech();
        plainTextOutputSpeech.setText(message);

        if (askResponse)
        {
            PlainTextOutputSpeech rePromptOutputSpeech = new PlainTextOutputSpeech();
            rePromptOutputSpeech.setText(rePromptMessage);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(rePromptOutputSpeech);

            return SpeechletResponse.newAskResponse(plainTextOutputSpeech,reprompt,simpleCard);
        }
        else
        {
            return SpeechletResponse.newTellResponse(plainTextOutputSpeech,simpleCard);
        }
    }
}
