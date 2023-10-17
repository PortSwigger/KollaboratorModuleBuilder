package kollaborator.module.builder.poller;

import burp.api.montoya.collaborator.CollaboratorClient;
import burp.api.montoya.collaborator.Interaction;
import kollaborator.module.builder.MyInteractionHandler;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Poller {
	private final CollaboratorClient collaboratorClient;
    private final List<MyInteractionHandler> interactionHandlers;
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private ScheduledFuture<?> schedule;

    public Poller(CollaboratorClient collaboratorClient, Duration pollInterval)
    {
        this.collaboratorClient = collaboratorClient;
        this.interactionHandlers = new LinkedList<>();
    }

    public void registerInteractionHandler(MyInteractionHandler interactionHandler)
    {
        interactionHandlers.add(interactionHandler);
    }

    public void start()
    {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(new PollingRunnable(), 0, 2, TimeUnit.SECONDS);
    }

    public void shutdown()
    {
        schedule.cancel(true);
        scheduledThreadPoolExecutor.shutdown();
    }

    private class PollingRunnable implements Runnable
    {
        public void run()
        {
            List<Interaction> interactionList = collaboratorClient.getAllInteractions();
            for (Interaction interaction : interactionList)
            {
                for (MyInteractionHandler interactionHandler : interactionHandlers)
                {
                    interactionHandler.handleInteraction(interaction);
                }
            }
        }
    }
}
