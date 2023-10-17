package kollaborator.module.builder;


import java.util.List;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.Interaction;

public class InteractionLogger {
    private final MontoyaApi api;

    public InteractionLogger(MontoyaApi api)
    {
        this.api = api;
    }

    public void logInteractions(List<Interaction> allInteractions)
    {
        api.logging().logToOutput(allInteractions.size() + " unread interactions.");

        for (Interaction interaction : allInteractions)
        {
            logInteraction(interaction);
        }
    }

    public void logInteraction(Interaction interaction)
    {
        api.logging().logToOutput(" Interaction type: " + interaction.type().name() +"\n  Interaction ID: " + interaction.id() );
    }

}
