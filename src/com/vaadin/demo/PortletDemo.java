/**
 * 
 */
package com.vaadin.demo;

import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.PortletApplicationContext;
import com.vaadin.terminal.gwt.server.PortletApplicationContext.PortletListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

/**
 * @author marc
 * 
 */
@SuppressWarnings("serial")
public class PortletDemo extends Application {

    Window main = new Window();
    TextField tf = new TextField("Some value");
    Label userInfo = new Label();
    Link portletEdit = new Link();
    Link portletMax = new Link();
    Link someAction = null;

    @Override
    public void init() {
        main = new Window();
        setMainWindow(main);

        userInfo.setCaption("User info");
        userInfo.setContentMode(Label.CONTENT_PREFORMATTED);
        main.addComponent(userInfo);

        tf.setEnabled(false);
        tf.setImmediate(true);
        main.addComponent(tf);

        portletEdit.setEnabled(false);
        main.addComponent(portletEdit);
        portletMax.setEnabled(false);
        main.addComponent(portletMax);

        if (getContext() instanceof PortletApplicationContext) {
            PortletApplicationContext ctx = (PortletApplicationContext) getContext();
            ctx.addPortletListener(this, new DemoPortletListener());
        } else {
            getMainWindow().showNotification("Not inited via Portal!",
                    Notification.TYPE_ERROR_MESSAGE);
        }

    }

    private class DemoPortletListener implements PortletListener {

        public void handleActionRequest(ActionRequest request,
                ActionResponse response) {

            main.addComponent(new Label("Action received"));

        }

        @SuppressWarnings("unchecked")
        public void handleRenderRequest(RenderRequest request,
                RenderResponse response) {
            // Portlet up-and-running, enable stuff
            portletEdit.setEnabled(true);
            portletMax.setEnabled(true);

            // Editable if we're in editmode
            tf.setEnabled((request.getPortletMode() == PortletMode.EDIT));

            // Show notification about current mode and state
            getMainWindow().showNotification(
                    "Portlet status",
                    "Mode: " + request.getPortletMode() + " State: "
                            + request.getWindowState(),
                    Notification.TYPE_WARNING_MESSAGE);

            // Display current user info
            Map uinfo = (Map) request.getAttribute(PortletRequest.USER_INFO);
            if (uinfo != null) {
                String s = "";
                for (Iterator it = uinfo.keySet().iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object val = uinfo.get(key);
                    s += key + ": " + val + "\n";
                }
                if (request.isUserInRole("administrator")) {
                    s += "(administrator)";
                }
                userInfo.setValue(s);
            } else {
                userInfo.setValue("-");
            }

            // Create Edit/Done link (actionUrl)
            PortletURL url = response.createActionURL();
            try {
                url
                        .setPortletMode((request.getPortletMode() == PortletMode.VIEW ? PortletMode.EDIT
                                : PortletMode.VIEW));
                portletEdit.setResource(new ExternalResource(url.toString()));
                portletEdit
                        .setCaption((request.getPortletMode() == PortletMode.VIEW ? "Edit"
                                : "Done"));
            } catch (Exception e) {
                portletEdit.setEnabled(false);
            }
            // Create Maximize/Normal link (actionUrl)
            url = response.createActionURL();
            try {
                url
                        .setWindowState((request.getWindowState() == WindowState.NORMAL ? WindowState.MAXIMIZED
                                : WindowState.NORMAL));
                portletMax.setResource(new ExternalResource(url.toString()));
                portletMax
                        .setCaption((request.getWindowState() == WindowState.NORMAL ? "Maximize"
                                : "Back to normal"));
            } catch (Exception e) {
                portletMax.setEnabled(false);
            }

            if (someAction == null) {
                url = response.createActionURL();
                try {
                    someAction = new Link("An action", new ExternalResource(url
                            .toString()));
                    main.addComponent(someAction);
                } catch (Exception e) {
                    // Oops
                    System.err.println("Could not create someAction: " + e);
                }

            }

        }
    }
}
