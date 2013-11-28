package org.jahia.modules.external.modules.osgi;

import org.apache.commons.lang.StringUtils;
import org.jahia.bundles.extender.jahiamodules.BundleHttpResourcesTracker;
import org.jahia.bundles.extender.jahiamodules.FileHttpContext;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModulesSourceHttpServiceTracker extends BundleHttpResourcesTracker {
    private static Logger logger = LoggerFactory.getLogger(ModulesSourceHttpServiceTracker.class);

    private final JahiaTemplatesPackage module;
    private final BundleScriptResolver bundleScriptResolver;
    private final TemplatePackageRegistry templatePackageRegistry;
    private HttpService httpService;

    public ModulesSourceHttpServiceTracker(JahiaTemplatesPackage module) {
        super(module.getBundle());
        this.module = module;
        this.bundleScriptResolver = (BundleScriptResolver) SpringContextSingleton.getBean("BundleScriptResolver");
        this.templatePackageRegistry = (TemplatePackageRegistry) SpringContextSingleton.getBean("org.jahia.services.templates.TemplatePackageRegistry");
    }

    @Override
    public Object addingService(ServiceReference reference) {
        HttpService httpService = (HttpService) context.getService(reference);
        this.httpService = httpService;
        return httpService;
    }

    public void registerJsp(String jspPath) {
        if (bundle.getEntry(jspPath) != null) {
            return;
        }
        String jspServletAlias = "/" + bundle.getSymbolicName() + jspPath;
        HttpContext httpContext = new FileHttpContext(FileHttpContext.getSourceURLs(bundle),
                httpService.createDefaultHttpContext());
        registerJspServlet(httpService, httpContext, jspServletAlias, jspPath, null);
        bundleScriptResolver.addBundleScript(bundle, jspPath);
        templatePackageRegistry.addModuleWithViewsForComponent(StringUtils.substringBetween(jspPath, "/", "/"), module);
        if (logger.isDebugEnabled()) {
            logger.debug("Register JSP {} in bundle {}", jspPath, bundleName);
        }
    }

    public void unregisterJsp(String jspPath) {
        String jspServletAlias = "/" + bundle.getSymbolicName() + jspPath;
        httpService.unregister(jspServletAlias);
        bundleScriptResolver.removeBundleScript(bundle, jspPath);
        templatePackageRegistry.removeModuleWithViewsForComponent(StringUtils.substringBetween(jspPath, "/", "/"), module);
        if (logger.isDebugEnabled()) {
            logger.debug("Unregister JSP {} in bundle {}", jspPath, bundleName);
        }
    }
}
