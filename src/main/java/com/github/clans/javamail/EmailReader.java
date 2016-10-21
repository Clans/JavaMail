package com.github.clans.javamail;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

public class EmailReader {

    private int level = 0;

    public void readEmails(String username, String password, boolean saveAttachments) {
        String host = "imap.gmail.com";
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.imap.ssl.enable", "true");
//            props.put("mail.pop3.host", host);
//            props.put("mail.imap.port", "993");
//            props.put("mail.pop3.starttls.enable", "true");

            Session session = Session.getDefaultInstance(props, null);

            Store store = session.getStore("imaps");
            store.connect(host, username, password);

            //create the folder object and open it
            Folder inbox = store.getFolder("INBOX");
            if (!inbox.exists()) return;

            inbox.open(Folder.READ_ONLY);

            int messageCount = inbox.getMessageCount();

            // Retrieve the messages from the folder in an array and print it

            // Retrieve all messages
//            Message[] messages = inbox.getMessages();

            // Retrieve only unread messages
//            Message messages[] = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            // Retrieve only 5 newest messages
            Message[] messages = inbox.getMessages(messageCount - 3, messageCount);

            System.out.println("Number of received emails: " + messages.length);
            System.out.println("Number of unread emails: " + inbox.getUnreadMessageCount());
            System.out.println("Total number of emails: " + messageCount);

            for (Message message : messages) {
                printMessage(message, saveAttachments);
            }

            //close the store and folder objects
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printMessage(Message message, boolean saveAttachments) throws Exception {
        print("---------------------------------");
        Address[] addresses;
        // FROM
        if ((addresses = message.getFrom()) != null) {
            String senders = Arrays.stream(addresses)
                    .map(Address::toString)
                    .collect(Collectors.joining(", "));
            print("FROM: " + senders);
        }

        // TO
        if ((addresses = message.getRecipients(Message.RecipientType.TO)) != null) {
            String recipients = Arrays.stream(addresses)
                    .map(Address::toString)
                    .collect(Collectors.joining(", "));
            print("TO: " + recipients);
        }

        String subject = message.getSubject();
        Date receivedDate = message.getReceivedDate();
        print("SUBJECT: " + subject);
        print("RECEIVED DATE: " + receivedDate.toString());
        print("CONTENT-TYPE: " + message.getContentType());
        dumpPart(message, saveAttachments);
    }

    public void dumpPart(Part p, boolean saveAttachments) throws Exception {
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            level++;
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                dumpPart(mp.getBodyPart(i), saveAttachments);
            level--;
        } else if (p.isMimeType("message/rfc822")) {
            print("--- This is a Nested Message (" + p.getContentType() + "):");
            level++;
            dumpPart((Part) p.getContent(), saveAttachments);
            level--;
        } else if (p.isMimeType("text/*")) {
            print("MESSAGE (" + p.getContentType() + "):");
            print((String) p.getContent());
        } else {
            /*
             * If we actually want to see the data, and it's not a
             * MIME type we know, fetch it and check its Java type.
             */
            print("MESSAGE (" + p.getContentType() + "):");
            String filename = p.getFileName();
            if (filename != null) {
                print("FILENAME: " + filename);
            }

            Object o = p.getContent();
            if (o instanceof String) {
                System.out.println((String) o);
            } else if (o instanceof InputStream && !saveAttachments) {
                print("--- This is just an input stream ---");
                InputStream is = (InputStream) o;
                int c;
                while ((c = is.read()) != -1)
                    System.out.write(c);
            }
        }

        /*
         * If we're saving attachments, write out anything that
         * looks like an attachment into an appropriately named
         * file.  Don't overwrite existing files to prevent
         * mistakes.
         */
        if (saveAttachments && level != 0 && p instanceof MimeBodyPart
                && !p.isMimeType("multipart/*") && !p.isMimeType("text/*")) {
            int attnum = 1;
            String filename = p.getFileName();
            String disp = p.getDisposition();
            // many mailers don't include a Content-Disposition
            if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                if (filename == null) {
                    filename = "Attachment" + attnum++;
                }
                try {
                    File f = new File(filename);
                    while (f.exists()) {
                        int pos = filename.lastIndexOf(".");
                        if (pos > 0) {
                            String substring = filename.substring(0, pos);
                            filename = substring + "_" + attnum++ + filename.substring(pos, filename.length());
                        } else {
                            filename += attnum++;
                        }

                        f = new File(filename);
                    }

                    print("Saving attachment to file " + filename);
                    ((MimeBodyPart) p).saveFile(f);
                } catch (IOException ex) {
                    print("Failed to save attachment: " + ex);
                }
            }
        }
    }

    private void print(String s) {
        System.out.println(s);
    }
}
