package com.racingprodigy.standings_bot.config;

import com.racingprodigy.standings_bot.commands.CheckStandingsCommand;
import com.racingprodigy.standings_bot.commands.UploadDriverCsvCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class JDAConfig {

    @Value("${discord.token}")
    private String token;

    @Bean()
    @Scope("singleton")
    public JDA jda(UploadDriverCsvCommand uploadDriverCsvCommand,
                   CheckStandingsCommand checkStandingsCommand) {
        var api = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(uploadDriverCsvCommand, checkStandingsCommand).build();

//        api.retrieveCommands().queue(commands -> {
//            for (var command : commands) {
//                api.deleteCommandById(command.getId()).queue();
//            }
//        });

        api.upsertCommand(
                Commands.slash("uploadmx5list", "Upload list")
                        .addOption(OptionType.ATTACHMENT, "csvfile", "csvfile")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        ).queue();

        api.upsertCommand(
                Commands.slash("checkstandings", "Check your standings in the RP Leaderboards")
                        .addOptions(new OptionData(OptionType.STRING,
                                        "championship",
                                        "Which championship are you checking",
                                        true)
                                        .addChoice("Fanatec Global Mazda MX-5 Cup", "MX-5")
                                        .addChoice("PRL Toyota GR86 Cup", "GR86"),
                                new OptionData(OptionType.INTEGER, "iracingid", "Please enter your iRacing ID", true))
        ).queue();

        return api;
    }
}
