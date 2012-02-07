package au.gov.ga.worldwind.common.util.transform;

public class RegexURLTransform implements URLTransform
{
	private final String regex;
	private final String replacement;

	public RegexURLTransform(String regex, String replacement)
	{
		this.regex = regex;
		this.replacement = replacement;
	}

	@Override
	public String transformURL(String url)
	{
		if (url == null)
		{
			return url;
		}
		return url.replaceAll(regex, replacement);
	}
}
