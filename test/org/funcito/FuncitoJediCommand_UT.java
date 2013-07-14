package org.funcito;

import jedi.functional.Command;
import org.funcito.internal.FuncitoDelegate;
import org.funcito.internal.WrapperType;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static org.funcito.FuncitoJedi.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyright 2013 Project Funcito Contributors
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class FuncitoJediCommand_UT {

    public @Rule ExpectedException thrown = ExpectedException.none();
    private Grows CALLS_TO_GROWS = callsTo(Grows.class);

    @After
    public void tearDown() {
        try {
            new FuncitoDelegate().extractInvokableState(WrapperType.JEDI_VOID_COMMAND);
        } catch (Throwable t) {}
    }

    public class Grows {
        int i = 0;
        public String incAndReturn() {
            i++;
            return Integer.toString(i);
        }
        public void inc() {
            i++;
        }
        public void dec() {
            i--;
        }
    }

    @Test
    public void testCommandFor_AssignToCommandWithSourceSuperType() {
        Grows grows = new Grows();

        Command<Object> superTypeRet = commandFor(CALLS_TO_GROWS.incAndReturn()); // Generic type is Object instead of Grows
        assertEquals(0, grows.i);

        superTypeRet.execute(grows);
        assertEquals(1, grows.i);
    }

    class Generic<N extends Number> {
        Double number;
        public Generic(N n) { number = n.doubleValue(); }
        public Double incAndGet() {
            return ++number;
        }
        public void voidInc() {
            ++number;
        }
    }

    @Test
    public void testCommandFor_ValidateDetectsMismatchedGenericTypes() {
        Command<Generic<Float>> floatGenericCommand = commandFor(callsTo(Generic.class).incAndGet());
        Generic<Integer> integerGeneric = new Generic<Integer>(0);

//        The below can't actually be compiled, which proves the test passes: compile time mismatch detection
//        floatGenericCommand.f(integerGeneric);
    }

    @Test
    public void testCommandFor_AllowUpcastToExtensionGenericType() {
        Command<Generic<? extends Object>> incCommand = commandFor(callsTo(Generic.class).incAndGet());
        Generic<Integer> integerGeneric = new Generic<Integer>(0);
        assertEquals(0, integerGeneric.number, 0.01);

        incCommand.execute(integerGeneric);

        assertEquals(1.0, integerGeneric.number, 0.01);
    }

    @Test
    public void testCommandFor_SingleArgBinding() {
        class IncList extends ArrayList<Integer> {
            public int incIndex(int i) {
                int oldVal = this.get(i);
                int newVal = oldVal + 1;
                this.set(i, newVal);
                return newVal;
            }
        }
        IncList callsToIncList = callsTo(IncList.class);
        Command<IncList> incElem0Func = commandFor(callsToIncList.incIndex(0));
        Command<IncList> incElem2Func = commandFor(callsToIncList.incIndex(2));
        IncList list = new IncList();
        list.add(0); list.add(100); list.add(1000);

        incElem0Func.execute(list);
        incElem2Func.execute(list);

        assertEquals(1, list.get(0).intValue());
        assertEquals(100, list.get(1).intValue()); // unchanged
        assertEquals(1001, list.get(2).intValue());
    }

    @Test
    public void testVoidCommand_withPrepare() {
        Grows grows = new Grows();

        prepareVoid(CALLS_TO_GROWS).inc();
        Command<Grows> normalCall = voidCommand();

        assertEquals(0, grows.i);
        normalCall.execute(grows);
        assertEquals(1, grows.i);
    }

    @Test
    public void testVoidCommand_withoutPrepare() {
        Grows grows = new Grows();

        // non-preferred.  Better to use prepare() to help explain
        CALLS_TO_GROWS.inc();
        Command<Grows> normalCall = voidCommand();

        assertEquals(0, grows.i);
        normalCall.execute(grows);
        assertEquals(1, grows.i);
    }

    @Test
    public void testVoidCommand_prepareWithNoMethodCall() {
        prepareVoid(CALLS_TO_GROWS); // did not append any ".methodCall()" after close parenthesis

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("No call to a");
        Command<Grows> badCall = voidCommand();
    }

    @Test
    public void testVoidCommand_AssignToCommandWithSourceSuperTypeOk() {
        Grows grows = new Grows();

        prepareVoid(CALLS_TO_GROWS).inc();
        Command<Object> superTypeRet = voidCommand(); // Generic type is Object instead of Grows
        assertEquals(0, grows.i);

        superTypeRet.execute(grows);
        assertEquals(1, grows.i);
    }

    @Test
    public void testVoidCommand_unsafeAssignment() {
        prepareVoid(CALLS_TO_GROWS).inc();
        Command<Integer> unsafe = voidCommand();  // unsafe assignment compiles

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("Method inc() does not exist");
        unsafe.execute(3); // invocation target type does not match prepared target type
    }

    @Test
    public void testVoidCommand_typeValidationSucceeds() {
        prepareVoid(CALLS_TO_GROWS).inc();

        Command<Grows> grows = voidCommand(Grows.class);
    }

    @Test
    public void testVoidCommand_typeValidationSucceedsWithSuperClass() {
        class Grows2 extends Grows{}
        prepareVoid(callsTo(Grows2.class)).inc();

        Command<Grows> grows = voidCommand(Grows.class);
    }

    @Test
    public void testVoidCommand_typeValidationFails() {
        prepareVoid(CALLS_TO_GROWS).inc();

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("Failed to create Jedi Command");
        Command<?> e = voidCommand(Number.class);  // type validation
    }


    @Test
    public void testVoidCommand_typeValidationFailsButLeavesInvokableStateUnchanged() {
        prepareVoid(CALLS_TO_GROWS).inc();

        try {
            Command<?> e = voidCommand(Number.class);  // type validation should fail
            fail("should have thrown exception");
        } catch (FuncitoException fe) {
            Command<Grows> g = voidCommand(Grows.class);  // type validation ok
        }
    }

    @Test
    public void testVoidCommand_badOrderOfPrepares() {
        // First call below requires a subsequent call to voidCommand() before another prepareVoid()
        prepareVoid(CALLS_TO_GROWS).inc();

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("or back-to-back \"prepareVoid()\" calls");
        // bad to call prepareVoid() twice without intermediate voidCommand()
        prepareVoid(CALLS_TO_GROWS).dec();
        // see clean-up in method tearDown()
    }

    @Test
    public void testVoidCommand_interleavedPreparesDifferentSourcesAlsoNotOk() {
        prepareVoid(CALLS_TO_GROWS).inc();

        thrown.expect(FuncitoException.class);
        thrown.expectMessage("or back-to-back \"prepareVoid()\" calls");
        prepareVoid(callsTo(Generic.class)).voidInc();
    }

    @Test
    public void testVoidCommand_preparedForNonVoidMethod() {
        Grows grows = new Grows();
        assertEquals(0, grows.i);

        prepareVoid(CALLS_TO_GROWS).incAndReturn();
        Command<Grows> e = voidCommand();

        e.execute(grows);

        assertEquals(1, grows.i);
    }
}
