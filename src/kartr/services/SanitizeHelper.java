package kartr.services;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class SanitizeHelper {
  public static final PolicyFactory policy = new HtmlPolicyBuilder().toFactory();

  public static String sanitize(String input) {
    return policy.sanitize(input);
  }
}
