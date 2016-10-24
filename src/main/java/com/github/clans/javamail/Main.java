package com.github.clans.javamail;

import org.apache.commons.cli.*;

public class Main {

    private static final String ACTION_SEND = "send";
    private static final String ACTION_READ = "read";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("a")
                .longOpt("action")
                .desc("The action to be executed '" + ACTION_SEND + "' or '" + ACTION_READ + "'.")
                .hasArg()
                .required()
                .build()
        );
        options.addOption("u", "username", true, "Username.");
        options.addOption("p", "password", true, "Password.");
        options.addOption("r", "recipient", true, "Recipient email.");
        options.addOption("A", "attachment", true, "Path to file to be attached. If not passed - default attachment will be used.");
        options.addOption("c", "count", true, "Number of messages to be read.");
        options.addOption(Option.builder("s").desc("Save attachments.").build());

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("a")) {
                String action = line.getOptionValue("a");
                String requiredOption = null;
                if (!line.hasOption("u")) {
                    requiredOption = "u";
                } else if (!line.hasOption("p")) {
                    requiredOption = "p";
                }

                String username = line.getOptionValue("u");
                String password = line.getOptionValue("p");

                if (action.equals(ACTION_SEND)) {
                    if (!line.hasOption("r")) {
                        requiredOption = "r";
                    }

                    if (requiredOption == null) {
                        String recipientEmail = line.getOptionValue("r");
                        String attachment = line.getOptionValue("A");
                        EmailSender emailSender = new EmailSender();
                        emailSender.sendEmail(username, password, recipientEmail, attachment);
                    } else {
                        throw new ParseException("Missing required option: " + requiredOption);
                    }
                } else if (action.equals(ACTION_READ)) {
                    if (requiredOption == null) {
                        int count = 0;
                        if (line.hasOption("c")) {
                            count = Integer.parseInt(line.getOptionValue("c"));
                        }
                        EmailReader emailReader = new EmailReader();
                        emailReader.readEmails(username, password, line.hasOption("s"), count);
                    }
                }
            }
        } catch (ParseException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            formatter.printHelp("javamail.jar", options, true);
        }
    }
}
