package org.ranobe.core.sources.en;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.ranobe.core.models.Chapter;
import org.ranobe.core.models.DataSource;
import org.ranobe.core.models.Filter;
import org.ranobe.core.models.Lang;
import org.ranobe.core.models.Novel;
import org.ranobe.core.network.HttpClient;
import org.ranobe.core.sources.Source;
import org.ranobe.core.util.NumberUtils;
import org.ranobe.core.util.SourceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WuxiaWorld implements Source {
    private static final String baseUrl = "https://wuxiaworld.site";
    private static final int sourceId = 13;

    private String cleanImg(String cover) {
        return cover.replaceAll("/-\\d+x\\d+.\\w{3}/gm", ".jpg");
    }

    @Override
    public DataSource metadata() {
        DataSource source = new DataSource();
        source.sourceId = sourceId;
        source.url = baseUrl;
        source.name = "Wuxia World";
        source.lang = Lang.eng;
        source.dev = "ap-atul";
        source.logo = "https://wuxiaworld.site/wp-content/uploads/2019/04/favicon-1.ico";
        source.isActive = true;
        return source;
    }

    @Override
    public List<Novel> novels(int page) throws IOException {
        List<Novel> items = new ArrayList<>();
        String web = baseUrl.concat("/page/").concat(String.valueOf(page));
        Element doc = Jsoup.parse(HttpClient.GET(web, new HashMap<>()));

        for (Element element : doc.select(".page-item-detail")) {
            String url = element.select(".h5 > a").attr("href").trim();

            if (url.length() > 0) {
                Novel item = new Novel(url);
                item.sourceId = sourceId;
                item.name = element.select(".h5 > a").text().trim();
                item.cover = cleanImg(element.select("img").attr("data-src").trim());
                items.add(item);
            }
        }

        return items;
    }

    @Override
    public Novel details(Novel novel) throws IOException {
        Element doc = Jsoup.parse(HttpClient.GET(novel.url, new HashMap<>()));

        novel.sourceId = sourceId;
        novel.name = doc.select(".post-title > h1").text().trim();
        novel.cover = cleanImg(doc.select(".summary_image > a > img").attr("data-src").trim());
        novel.summary = doc.select("div.summary__content").text().replaceAll("\n", "\n\n").trim();
        novel.rating = NumberUtils.toFloat(doc.select(".total_votes").text().trim());
        novel.authors = Arrays.asList(doc.select(".author-content > a").text().split(","));

        List<String> genres = new ArrayList<>();
        for (Element element : doc.select(".genres-content > a")) {
            genres.add(element.text().trim());
        }
        novel.genres = genres;

        for (Element element : doc.select(".post-content_item")) {
            String header = element.select(".summary-heading > h5").text().trim();
            String content = element.select(".summary-content").text().trim();

            if (header.equalsIgnoreCase("Status")) {
                novel.status = content;
            } else if (header.equalsIgnoreCase("Alternative")) {
                novel.alternateNames = Arrays.asList(content.split(","));
            } else if (header.equalsIgnoreCase("Release")) {
                novel.year = NumberUtils.toInt(content);
            }
        }

        return novel;
    }

    @Override
    public List<Chapter> chapters(Novel novel) throws IOException {
        List<Chapter> items = new ArrayList<>();
        String web = novel.url.concat("ajax/chapters");
        Element doc = Jsoup.parse(HttpClient.POST(web, new HashMap<>(), new HashMap<>()));

        for (Element element : doc.select(".wp-manga-chapter")) {
            Chapter item = new Chapter(novel.url);

            item.url = element.select("a").attr("href").trim();
            item.name = element.select("a").text().trim();
            item.id = NumberUtils.toFloat(item.name);
            item.updated = element.select("span.chapter-release-date").text().trim();
            items.add(item);
        }

        return items;
    }

    @Override
    public Chapter chapter(Chapter chapter) throws IOException {
        Element doc = Jsoup.parse(HttpClient.GET(chapter.url, new HashMap<>()));
        Element main = doc.select(".reading-content").first();

        if (main == null) {
            return null;
        }

        chapter.content = "";
        chapter.content = String.join("\n\n", main.select("p").eachText());
        return chapter;
    }

    @Override
    public List<Novel> search(Filter filters, int page) throws IOException {
        List<Novel> items = new ArrayList<>();

        if (filters.hashKeyword()) {
            String web = SourceUtils.buildUrl(baseUrl, "/page/", String.valueOf(page), "/?s=", filters.getKeyword(), "&post_type=wp-manga");
            Element doc = Jsoup.parse(HttpClient.GET(web, new HashMap<>()));
            for (Element element : doc.select(".c-tabs-item__content")) {
                String url = element.select(".tab-thumb  > a").attr("href").trim();

                if (url.length() > 0) {
                    Novel item = new Novel(url);
                    item.sourceId = sourceId;
                    item.url = url;
                    item.name = element.select(".post-title > h3 > a").text().trim();
                    item.cover = cleanImg(element.select("img.img-responsive").attr("data-src").trim());

                    items.add(item);
                }
            }
        }

        return items;
    }
}
