package org.jenkinsci.tools.remotecloner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.jenkinsci.tools.remotecloner.handler.Handler;
import org.jenkinsci.tools.remotecloner.handler.Usage;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.beust.jcommander.ParameterException;

@PrepareForTest(Main.class)
public class MainTest extends PowerMockTestCase {
    
    private Main main;
    private CommandResponse inResponse;
    private ByteArrayOutputStream bout = new ByteArrayOutputStream();
    private ByteArrayOutputStream berr = new ByteArrayOutputStream();
    private PrintStream out = new PrintStream(bout);
    private PrintStream err = new PrintStream(berr);
    
    @BeforeMethod
    public void setUp() {
        
        inResponse = new CommandResponse(out, err);
        main = new Main(inResponse);
    }
  
    @Test
    public void getUsageWhenNoArgsProvided() {

        assertThat(dispatch(), instanceOf(Usage.class));
    }
    
    @Test
    public void getUsageWhenInvalidArgsProvided() {

        assertThat(dispatch("no-such-command"), instanceOf(Usage.class));
    }
    
    private Handler dispatch(final String... args) {
        
        return main.getHandler(args);
    }

    @Test
    public void failedValidationShouldInvokeUsage() {
        
        final Handler handler = PowerMockito.mock(Handler.class);
        Mockito.doThrow(new ParameterException("Fake Exception")).when(handler).validate();
        
        final Main main = PowerMockito.spy(this.main);
        PowerMockito.when(main.getHandler("global")).thenReturn(handler);

        final int code = main.run("global").returnCode();
        
        assertThat(code, not(equalTo(0)));
        
        Mockito.verify(handler).validate();
        Mockito.verify(handler, Mockito.never()).run(inResponse);
        
        assertThat(stderr(), containsString("Fake Exception"));
        assertThat(stdout(), containsString("Usage: remote-cloner [options] [command] [command options]"));
    }
        
    private String stderr() {
        
        try {
         
            return berr.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        return null;
    }
    
    private String stdout() {
        
        try {
         
            return bout.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            
            e.printStackTrace();
            fail(e.getMessage());
        }
        
        return null;
    }
}
