import json
import asyncio
from datetime import datetime
from flask import Flask, request, jsonify
from playwright.async_api import async_playwright

app = Flask(__name__)

# 保存淘宝会话
async def save_taobao_session():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context()
        page = await context.new_page()
        await page.goto("https://www.taobao.com")
        print("请手动登录淘宝，登录完成后按回车继续...")
        input()  # 等待用户手动登录
        cookies = await context.cookies()
        with open("taobao_cookies.json", "w", encoding="utf-8") as f:
            json.dump(cookies, f, ensure_ascii=False, indent=4)
        print("Cookies 已保存到 taobao_cookies.json 文件！")
        await browser.close()

# 保存京东会话
async def save_jd_session():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context()
        page = await context.new_page()
        await page.goto("https://passport.jd.com/new/login.aspx")
        print("请手动登录京东，登录完成后按回车继续...")
        input()  # 等待用户手动登录
        cookies = await context.cookies()
        with open("jd_cookies.json", "w", encoding="utf-8") as f:
            json.dump(cookies, f, ensure_ascii=False, indent=4)
        print("Cookies 已保存到 jd_cookies.json 文件！")
        await browser.close()

# 爬取淘宝商品信息
async def scrape_taobao(query):
    results = []
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        try:
            with open("taobao_cookies.json", "r") as f:
                cookies = json.load(f)
                await context.add_cookies(cookies)
        except FileNotFoundError:
            print("淘宝 Cookie 文件不存在，开始保存会话...")
            await save_taobao_session()
            with open("taobao_cookies.json", "r") as f:
                cookies = json.load(f)
                await context.add_cookies(cookies)
        page = await context.new_page()
        await page.goto(f"https://s.taobao.com/search?q={query}", timeout=60000)
        await page.wait_for_selector('a.doubleCardWrapperAdapt--mEcC7olq', timeout=60000)
        for _ in range(5):
            await page.mouse.wheel(0, 1000)
            await asyncio.sleep(1)
        items = await page.query_selector_all('a.doubleCardWrapperAdapt--mEcC7olq')
        for item in items:
            try:
                title_element = await item.query_selector('div.title--qJ7Xg_90 span')
                title = await title_element.inner_text() if title_element else "N/A"
                price_int_element = await item.query_selector('span.priceInt--yqqZMJ5a')
                price_float_element = await item.query_selector('span.priceFloat--XpixvyQ1')
                price_int = await price_int_element.inner_text() if price_int_element else "0"
                price_float = await price_float_element.inner_text() if price_float_element else ".00"
                price = f"{price_int}{price_float}"
                link = await item.get_attribute('href')
                img_element = await item.query_selector('img')
                img_url = await img_element.get_attribute('src') if img_element else "N/A"
                results.append({
                    "title": title.strip(),
                    "price": price.strip(),
                    "link": "https:" + link.strip() if link.startswith("//") else link.strip(),
                    "image": "https:" + img_url.strip() if img_url.startswith("//") else img_url.strip(),
                    "source": "淘宝",
                    "date": datetime.now().strftime("%Y-%m-%d")  # 添加查询日期
                })
            except Exception as e:
                print(f"Error parsing Taobao item: {e}")
        await browser.close()
    return results

# 爬取京东商品信息
async def scrape_jd(query):
    results = []
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        context = await browser.new_context()
        try:
            with open("jd_cookies.json", "r") as f:
                cookies = json.load(f)
                await context.add_cookies(cookies)
        except FileNotFoundError:
            print("京东 Cookie 文件不存在，开始保存会话...")
            await save_jd_session()
            with open("jd_cookies.json", "r") as f:
                cookies = json.load(f)
                await context.add_cookies(cookies)
        page = await context.new_page()
        await page.goto(f"https://search.jd.com/Search?keyword={query}", timeout=60000)
        await page.wait_for_selector('.gl-item', timeout=60000)
        for _ in range(5):
            await page.mouse.wheel(0, 1000)
            await asyncio.sleep(1)
        items = await page.query_selector_all('.gl-item')
        for item in items:
            try:
                title_element = await item.query_selector('.p-name em')
                title = await title_element.inner_text() if title_element else "N/A"
                price_element = await item.query_selector('.p-price i')
                price = await price_element.inner_text() if price_element else "N/A"
                link_element = await item.query_selector('.p-name a')
                link = await link_element.get_attribute('href') if link_element else "N/A"
                img_element = await item.query_selector('.p-img img')
                img_url = await img_element.get_attribute('src') if img_element else "N/A"
                if img_url.startswith("//"):
                    img_url = "https:" + img_url
                if link.startswith("//"):
                    link = "https:" + link
                results.append({
                    "title": title.strip(),
                    "price": price.strip(),
                    "link": link.strip(),
                    "image": img_url.strip(),
                    "source": "京东",
                    "date": datetime.now().strftime("%Y-%m-%d")  # 添加查询日期
                })
            except Exception as e:
                print(f"Error parsing JD item: {e}")
        await browser.close()
    return results

# 合并爬虫接口
@app.route('/scrape', methods=['GET'])
async def scrape():
    query = request.args.get('query')
    if not query:
        return jsonify({"error": "Query parameter is required"}), 400
    try:
        taobao_results = await scrape_taobao(query)
        jd_results = await scrape_jd(query)
        combined_results = taobao_results + jd_results
        return jsonify(combined_results)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000)
