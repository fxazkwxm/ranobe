package org.ranobe.ranobe.sources;

import org.ranobe.ranobe.sources.en.AllNovel;
import org.ranobe.ranobe.sources.en.AzyNovel;
import org.ranobe.ranobe.sources.en.BoxNovel;
import org.ranobe.ranobe.sources.en.FreeWebNovel;
import org.ranobe.ranobe.sources.en.LightNovelBtt;
import org.ranobe.ranobe.sources.en.LightNovelHeaven;
import org.ranobe.ranobe.sources.en.LightNovelPub;
import org.ranobe.ranobe.sources.en.LightNovelWorld;
import org.ranobe.ranobe.sources.en.Neovel;
import org.ranobe.ranobe.sources.en.NewNovel;
import org.ranobe.ranobe.sources.en.NovelBin;
import org.ranobe.ranobe.sources.en.Ranobe;
import org.ranobe.ranobe.sources.en.ReadLightNovel;
import org.ranobe.ranobe.sources.en.ReadWebNovels;
import org.ranobe.ranobe.sources.en.VipNovel;
import org.ranobe.ranobe.sources.en.WordRain69;
import org.ranobe.ranobe.sources.en.WuxiaWorld;
import org.ranobe.ranobe.sources.ru.RanobeHub;

import java.util.HashMap;
import java.util.Map;

public class SourceManager {
    private SourceManager() throws IllegalAccessException {
        throw new IllegalAccessException("Cannot initialize this class ;)");
    }

    public static Source getSource(int sourceId) {
        try {
            Class<?> klass = getSources().get(sourceId);
            if (klass == null) {
                throw new ClassNotFoundException("Source not found with source id : " + sourceId);
            }
            return (Source) klass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return new ReadLightNovel();
        }
    }

    public static Map<Integer, Class<?>> getSources() {
        HashMap<Integer, Class<?>> sources = new HashMap<>();
        sources.put(1, ReadLightNovel.class);
        sources.put(2, VipNovel.class);
        sources.put(3, LightNovelBtt.class);
        sources.put(4, LightNovelPub.class);
        sources.put(5, RanobeHub.class);
        sources.put(6, Ranobe.class);
        sources.put(7, AllNovel.class);
        sources.put(8, AzyNovel.class);
        sources.put(9, LightNovelHeaven.class);
        sources.put(10, NewNovel.class);
        sources.put(11, ReadWebNovels.class);
        sources.put(12, BoxNovel.class);
        sources.put(13, WuxiaWorld.class);
        sources.put(14, Neovel.class);
        sources.put(15, LightNovelWorld.class);
        sources.put(16, FreeWebNovel.class);
        sources.put(17, WordRain69.class);
        sources.put(18, NovelBin.class);

        return sources;
    }
}
