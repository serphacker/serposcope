/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */
package com.serphacker.serposcope.db.google;

import com.google.inject.Inject;
import com.serphacker.serposcope.db.AbstractDBIT;
import com.serphacker.serposcope.db.base.BaseDB;
import com.serphacker.serposcope.models.base.Group;
import com.serphacker.serposcope.models.base.Run;
import com.serphacker.serposcope.models.google.GoogleSearch;
import com.serphacker.serposcope.models.google.GoogleSerp;
import com.serphacker.serposcope.models.google.GoogleSerpEntry;
import it.unimi.dsi.fastutil.shorts.Short2ShortArrayMap;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.unitils.reflectionassert.ReflectionAssert;

/**
 *
 * @author admin
 */
public class GoogleSerpDBIT extends AbstractDBIT {

    public GoogleSerpDBIT() {
    }

    ThreadLocalRandom r = ThreadLocalRandom.current();

    @Inject
    BaseDB baseDB;

    @Inject
    GoogleDB googleDB;

    @Test
    public void test() {

        Group grp = new Group(Group.Module.GOOGLE, "google group");
        baseDB.group.insert(grp);

        GoogleSearch search = new GoogleSearch("my keyword");
        googleDB.search.insert(Arrays.asList(search), grp.getId());

        Run run = new Run(Run.Mode.MANUAL, Group.Module.GOOGLE, LocalDateTime.now().withNano(0));
        baseDB.run.insert(run);

        GoogleSerp serp = new GoogleSerp(run.getId(), search.getId(), run.getStarted());
        for (int i = 0; i < 10; i++) {
            GoogleSerpEntry entry = new GoogleSerpEntry("url-" + i);
            entry.getMap().put((short) 1, (short) r.nextInt(Short.MAX_VALUE));
            entry.getMap().put((short) 7, (short) r.nextInt(Short.MAX_VALUE));
            entry.getMap().put((short) 30, (short) r.nextInt(Short.MAX_VALUE));
            entry.getMap().put((short) 90, (short) r.nextInt(Short.MAX_VALUE));
            serp.addEntry(entry);
        }

        googleDB.serp.insert(serp);

        GoogleSerp fetchedSerp = googleDB.serp.get(run.getId(), search.getId());

        System.out.println(fetchedSerp.getEntries());

        ReflectionAssert.assertReflectionEquals(serp, fetchedSerp);

    }

    @Test
    public void testStream() {

        Group grp = new Group(Group.Module.GOOGLE, "google group");
        baseDB.group.insert(grp);

        GoogleSearch search = new GoogleSearch("my keyword");
        googleDB.search.insert(Arrays.asList(search), grp.getId());

        LocalDateTime startDate = LocalDateTime.of(2010, 10, 10, 10, 10);

        for (int date = 0; date < 100; date++) {
            Run run = new Run(Run.Mode.MANUAL, Group.Module.GOOGLE, startDate.plusDays(date));
            run.setFinished(run.getStarted().plusSeconds(1));
            baseDB.run.insert(run);

            GoogleSerp serp = new GoogleSerp(run.getId(), search.getId(), run.getStarted());
            for (int i = 0; i < 10; i++) {
                GoogleSerpEntry entry = new GoogleSerpEntry("url-" + i);
                entry.getMap().put((short) 1, (short) r.nextInt(Short.MAX_VALUE));
                entry.getMap().put((short) 7, (short) r.nextInt(Short.MAX_VALUE));
                entry.getMap().put((short) 30, (short) r.nextInt(Short.MAX_VALUE));
                entry.getMap().put((short) 90, (short) r.nextInt(Short.MAX_VALUE));
                serp.addEntry(entry);
            }

            googleDB.serp.insert(serp);
        }
        
        googleDB.serp.stream(30, 50, search.getId(), new Consumer<GoogleSerp>() {
            @Override
            public void accept(GoogleSerp t) {
                System.out.println(t.getRunId() + "|" + t.getEntries().size());
            }
        });

    }
    
    @Test
    public void testFFF() throws IOException {
        String raw = "http://www.banque.net/\n"
            + "http://www.banques-en-ligne.fr/\n"
            + "http://www.boursorama.com/banque-en-ligne/\n"
            + "http://www.comparatifdebanque.fr/\n"
            + "http://www.ingdirect.fr/\n"
            + "https://www.monabanq.com/\n"
            + "https://www.hellobank.fr/\n"
            + "https://www.fortuneo.fr/compte-bancaire\n"
            + "http://www.quechoisir.org/argent-assurance/banque-credit/service-bancaire/guide-d-achat-banque-en-ligne-bien-choisir-sa-banque-en-ligne\n"
            + "http://www.avisbanque.net/\n"
            + "http://www.credit-agricole.fr/compte/banque-en-ligne/\n"
            + "http://www.credit-agricole.fr/professionnel/compte/banque-en-ligne/\n"
            + "http://www.banqueenligne.eu/\n"
            + "http://www.culturebanque.com/banques/banques-en-ligne/\n"
            + "https://e.secure.lcl.fr/\n"
            + "http://www.lefigaro.fr/conso/2015/04/28/05007-20150428ARTFIG00163-faut-il-vraiment-faire-confiance-aux-banques-en-ligne.php\n"
            + "http://www.cbanque.com/banque-en-ligne/\n"
            + "https://m.ca-nord-est.fr/\n"
            + "http://www.jechange.fr/placement/banque\n"
            + "http://www.banque-en-ligne.net/\n"
            + "https://www.bforbank.com/\n"
            + "http://www.lemonde.fr/argent/article/2015/10/24/banques-en-ligne-le-jeu-des-7-differences_4796173_1657007.html\n"
            + "https://fr.wikipedia.org/wiki/Banque_en_ligne\n"
            + "https://www.creditmutuel.fr/cmcee/fr/banques/particuliers/quotidien/suivre-et-gerer-vos-comptes/\n"
            + "https://www.labanquepostale.fr/\n"
            + "https://www.ca-nmp.fr/credit-agricole-en-ligne.html\n"
            + "https://www.banque-marze.fr/\n"
            + "https://www.vaguenligne.credit-maritime.fr/\n"
            + "https://www.cafedelabourse.com/archive/article/comparatif-offre-banque-en-ligne\n"
            + "http://www.comparatifbanques.com/\n"
            + "https://www.hsbc.fr/\n"
            + "http://www.lelynx.fr/banques/\n"
            + "https://www.barclays.fr/banque-en-ligne-@/1791-index.html\n"
            + "http://www.latribune.fr/entreprises-finance/banques-finance/20141128trib3cb5d34b4/la-banque-en-ligne-commence-enfin-a-decoller-en-france.html\n"
            + "https://www.caisse-epargne.fr/particuliers/banque-au-quotidien-banque-en-ligne.aspx\n"
            + "http://www.lesechos.fr/finance-marches/banque-assurances/021485601948-le-google-chinois-se-lance-dans-la-banque-en-ligne-1175965.php\n"
            + "http://www.mabanquenligne.com/\n"
            + "https://www.bpinet.fr/ma-banque-en-ligne\n"
            + "http://www.banqueenligne.org/\n"
            + "http://www.challenges.fr/entreprise/20131010.CHA5457/l-offensive-de-charme-des-banques-en-ligne.html\n"
            + "http://www.zdnet.fr/actualites/orange-se-lance-dans-la-banque-en-ligne-39822928.htm\n"
            + "http://www.rivesparis.banquepopulaire.fr/portailinternet/Editorial/BanqueEnLigne/Pages/consultation-comptes-en-ligne.aspx\n"
            + "http://www.choisir-sa-banque-en-ligne.com/\n"
            + "http://www.banquesenligne.pro/\n"
            + "http://droit-finances.commentcamarche.net/contents/1337-comment-choisir-sa-banque-en-ligne\n"
            + "http://www.panorabanques.com/choisir/banque-en-ligne/meilleures-banques-en-ligne\n"
            + "http://www.banque-du-net.com/\n"
            + "http://www.top10banques.com/\n"
            + "http://www.capital.fr/finances-perso/actualites/frais-bancaires-passez-a-la-banque-en-ligne-les-services-sont-le-plus-souvent-gratuits-1003376\n"
            + "https://particuliers.societegenerale.fr/";

        GoogleSerp serp = new GoogleSerp(1, 1, LocalDateTime.MIN);

        String[] lines = raw.split("\n");
        for (String line : lines) {
            serp.addEntry(new GoogleSerpEntry(line));
        }

        byte[] data = serp.getSerializedEntries();
        System.out.println("serialized : " + data.length);
        
        byte[] compressed = googleDB.serp.compress(data);
        System.out.println("compressed : " + compressed.length);
        
        byte[] decompressed = googleDB.serp.decompress(compressed);
        System.out.println("decompressed : " + decompressed.length);
        
        Assert.assertArrayEquals(data, decompressed);

    }

}
