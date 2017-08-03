package com.adarshr.raroscope

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class RarSpec extends Specification {

    def "scan rar file"() {
        when:
            def entries = new RARFile(new File('src/test/resources/archive.rar')).entries()
        then:
            while (entries.hasMoreElements()) {
                println entries.nextElement()
            }
    }
}
