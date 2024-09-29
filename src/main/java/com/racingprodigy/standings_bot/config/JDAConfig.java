package com.racingprodigy.standings_bot.config;

import com.racingprodigy.standings_bot.commands.UploadDriverCsvCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
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
    public JDA jda(UploadDriverCsvCommand uploadDriverCsvCommand) {
        var api = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(uploadDriverCsvCommand).build();


        /*api.retrieveCommands().queue(commands -> {
            for (var command : commands) {
                api.deleteCommandById(command.getId()).queue();
            }
        });*/

        api.upsertCommand(
                Commands.slash("uploadlist", "Upload list")
                        .addOption(OptionType.ATTACHMENT, "csvfile", "csvfile")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        ).queue();

        return api;
    }
}
