package kollaborator.module.builder;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.CollaboratorClient;
import burp.api.montoya.collaborator.SecretKey;
import burp.api.montoya.core.Registration;
import burp.api.montoya.persistence.PersistedObject;
import kollaborator.module.builder.poller.Poller;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Duration;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class KollaboratorModule implements BurpExtension{
	
	private MontoyaApi api;
	private Registration tab;
	public static JTextArea code; 

	public void initialize(MontoyaApi api) {
		this.api = api;

        api.extension().setName("Kollaborator Module Builder");
        
        

        CollaboratorClient collaboratorClient = createCollaboratorClient(api.persistence().extensionData());

        InteractionLogger interactionLogger = new InteractionLogger(api);
        interactionLogger.logInteractions(collaboratorClient.getAllInteractions());
        
        tab = api.userInterface().registerSuiteTab("KMB", createTab(collaboratorClient));
        
        // Periodically poll the CollaboratorClient to retrieve any new interactions.
        Poller collaboratorPoller = new Poller(collaboratorClient, Duration.ofSeconds(3));
        collaboratorPoller.registerInteractionHandler(new MyInteractionHandler(api, interactionLogger));
        collaboratorPoller.start();

        api.extension().registerUnloadingHandler(() ->
        {
            // Stop polling the CollaboratorClient.
            collaboratorPoller.shutdown();

            api.logging().logToOutput("Extension unloading...");
            
            tab.deregister();
        });
        
        
		
	}
	
	private CollaboratorClient createCollaboratorClient(PersistedObject persistedData)
    {
        CollaboratorClient collaboratorClient;

        String existingCollaboratorKey = persistedData.getString("persisted_collaborator");

        if (existingCollaboratorKey != null)
        {
            api.logging().logToOutput("Creating Collaborator client from key.");
            collaboratorClient = api.collaborator().restoreClient(SecretKey.secretKey(existingCollaboratorKey));
        }
        else
        {
            api.logging().logToOutput("No previously found Collaborator client. Creating new client...");
            collaboratorClient = api.collaborator().createClient();

            // Save the secret key of the CollaboratorClient so that you can retrieve it later.
            api.logging().logToOutput("Saving Collaborator secret key.");
            persistedData.setString("persisted_collaborator", collaboratorClient.getSecretKey().toString());
        }

        return collaboratorClient;
    }
	
	private Component createTab(CollaboratorClient collaboratorClient) {
		JButton getPayload = new JButton("Copy payload!"); 
		code = new JTextArea();
		JScrollPane scroll = new JScrollPane (code, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		getPayload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String payload = collaboratorClient.generatePayload().toString();
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(payload), null);
			}	
		});
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,getPayload, scroll);
		
		return splitPane;
		
	}

}
