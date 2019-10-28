package ttl.advjava.refplus.jit;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import static java.lang.System.out;

/**
 * @author whynot
 */
public class JOLDemo {

    public static void main(String [] args) {
        new JOLDemo().go();
    }

    public void go() {
        int [] arr = new int[10];

        out.println(VM.current().details());

        out.println(ClassLayout.parseClass(arr.getClass()).toPrintable());
    }
}
