package kollaborator.module.builder;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.collaborator.CollaboratorClient;
import burp.api.montoya.collaborator.SecretKey;
import burp.api.montoya.core.Registration;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.ui.swing.SwingUtils;
import kollaborator.module.builder.poller.Poller;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class KollaboratorModule implements BurpExtension{
	
	private MontoyaApi api;
	private Registration tab;
	static JTextArea code; 
	static JTextField timeout;
	static JCheckBox modifyRequests;

	public void initialize(MontoyaApi api) {
		this.api = api;

        api.extension().setName("Kollaborator Module Builder");
        api.http().registerHttpHandler(new MFASessionHandler(api));
        
        
        pythonCheck();
        
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
	
	private void pythonCheck() {
		Frame frame = api.userInterface().swingUtils().suiteFrame();
		
		try {
			Process process = Runtime.getRuntime().exec("python3 -V");
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			
			String line = null;
    		String err = null;
			
    		line = reader.readLine();
    		err = error.readLine();
    		
			if(line != null) {
				return;
			}else {
				JOptionPane.showMessageDialog(frame,
					    "This Burp extension requires Pyhton to be installed on your system and should be accessible via \"python\" command.\nPlease make sure that the python is at user's PATH. \nIn case you believe This is an error, Please log a new issue at \"https://github.com/mbkunal/KollaboratorModuleBuilder/issues\"\n\n\nCommon errors: python accessible via alias like python3 or python2 will also result in error.\nSimply run \"python\" on command line and verify if python is available",
					    "Python Error",
					    JOptionPane.PLAIN_MESSAGE);
			}
			reader.close();
			error.close();
			
		} catch (IOException e) {
			api.logging().logToError(e.getMessage());
			e.printStackTrace();
		}
		
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
		modifyRequests = new JCheckBox("Modify Requests");
		timeout = new JTextField("5",3);
		code = new JTextArea();
		JLabel timeoutLabel = new JLabel("    TimeOut (in seconds) ");
		timeoutLabel.setLabelFor(timeout);
		JPanel time = new JPanel(new BorderLayout());
		
		time.add(timeoutLabel,BorderLayout.WEST);
		time.add(timeout,BorderLayout.CENTER);
		
		JSeparator s = new JSeparator();
		
		s.setOrientation(SwingConstants.VERTICAL);
		
		JPanel params = new JPanel(); 
		params.add(modifyRequests );
		params.add(s);
		params.add( time );
		
		JSplitPane headerBar = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,getPayload,params);
		headerBar.setDividerLocation(0.5);
		headerBar.setResizeWeight(0.5);
		
		
		JScrollPane scroll = new JScrollPane (code, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		getPayload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String payload = collaboratorClient.generatePayload().toString();
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(payload), null);
			}	
		});
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,headerBar, scroll);
		
		return splitPane;
		
	}

}
