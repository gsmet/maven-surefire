package org.apache.maven.surefire.its;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import org.apache.maven.surefire.its.fixture.OutputValidator;
import org.apache.maven.surefire.its.fixture.SurefireVerifierTestClass;
import org.codehaus.plexus.util.FileUtils;

/**
 * Test charset provider (SUREFIRE-162)
 *
 * @author <a href="mailto:dfabulich@apache.org">Dan Fabulich</a>
 */
public class Surefire162CharsetProviderIT
    extends SurefireVerifierTestClass
{
    public Surefire162CharsetProviderIT()
    {
        super( "/surefire-162-charsetProvider" );
    }

    @SuppressWarnings( { "ResultOfMethodCallIgnored" } )
    public void testCharsetProvider()
        throws Exception
    {
        OutputValidator verifier = getDefaultVerifier();
        File jarFile = new File( verifier.getArtifactPath( "jcharset", "jcharset", "1.2.1", "jar" ) );
        jarFile.getParentFile().mkdirs();
        FileUtils.copyFile( getSubFile( "repo/jcharset/jcharset/1.2.1/jcharset-1.2.1.jar" ), jarFile );
        FileUtils.copyFile( getSubFile( "repo/jcharset/jcharset/1.2.1/jcharset-1.2.1.pom" ),
                            new File( verifier.getArtifactPath( "jcharset", "jcharset", "1.2.1", "pom" ) ) );
        executeTest();

        verifyErrorFreeLog();
        assertTestSuiteResults( 1, 0, 0, 0 );
    }
}
