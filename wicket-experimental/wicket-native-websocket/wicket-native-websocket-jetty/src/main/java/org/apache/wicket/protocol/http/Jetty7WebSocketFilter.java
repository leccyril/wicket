package org.apache.wicket.protocol.http;

import java.io.IOException;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.jetty.JettyWebSocketProcessor;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketFactory;

/**
 * An upgrade filter that uses Jetty's WebSocketFactory to decide whether to upgrade or not.
 */
public class Jetty7WebSocketFilter extends AbstractUpgradeFilter implements WebSocketFactory.Acceptor
{
	private WebSocketFactory _webSocketFactory;

	@Override
	public void init(final boolean isServlet, final FilterConfig filterConfig)
		throws ServletException
	{
		super.init(isServlet, filterConfig);

		try
		{
			String bs = filterConfig.getInitParameter("bufferSize");
			_webSocketFactory = new WebSocketFactory(this, bs == null ? 8192 : Integer.parseInt(bs));
			_webSocketFactory.start();

			String max = filterConfig.getInitParameter("maxIdleTime");
			if (max != null)
				_webSocketFactory.setMaxIdleTime(Integer.parseInt(max));

			max = filterConfig.getInitParameter("maxTextMessageSize");
			if (max != null)
				_webSocketFactory.setMaxTextMessageSize(Integer.parseInt(max));

			max = filterConfig.getInitParameter("maxBinaryMessageSize");
			if (max != null)
				_webSocketFactory.setMaxBinaryMessageSize(Integer.parseInt(max));
		}
		catch (ServletException x)
		{
			throw x;
		}
		catch (Exception x)
		{
			throw new ServletException(x);
		}
	}

	@Override
	protected boolean acceptWebSocket(HttpServletRequest req, HttpServletResponse resp, Application application) throws ServletException, IOException
	{
		return super.acceptWebSocket(req, resp, application) && _webSocketFactory.acceptWebSocket(req, resp);
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol)
	{
		JettyWebSocketProcessor webSocketHandler = new JettyWebSocketProcessor(request, getApplication());
		return webSocketHandler.new JettyWebSocket();
	}

	/* ------------------------------------------------------------ */
	@Override
	public boolean checkOrigin(HttpServletRequest request, String origin)
	{
		return true;
	}

	/* ------------------------------------------------------------ */
	@Override
	public void destroy()
	{
		try
		{
			_webSocketFactory.stop();
		}
		catch (Exception x)
		{
			x.printStackTrace();
		}

		super.destroy();
	}
}
