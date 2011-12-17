package org.funcito.jedi;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import jedi.functional.Filter;
import jedi.functional.Functor;
import org.funcito.guava.DefaultableMethodPredicate;
import org.funcito.internal.FuncitoDelegate;
import org.funcito.internal.Invokable;

import static org.funcito.internal.WrapperType.*;

/**
 * Copyright 2011 Project Funcito Contributors
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
public class JediDelegate extends FuncitoDelegate {
    public <T,V> Functor<T,V> functorFor(V ignoredRetVal) {
        final Invokable<T,V> invokable = getInvokable(JEDI_FUNCTOR);
        return new MethodFunctor<T, V>(invokable);
    }

    public <T> Filter<T> filterFor(Boolean ignoredRetVal) {
        final Invokable<T,Boolean> invokable = getInvokable(JEDI_FILTER);
        return new MethodFilter<T>(invokable);
    }
}