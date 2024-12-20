import json
import asyncio
from flask import Flask, request, jsonify
from playwright.async_api import async_playwright

app = Flask(__name__)

# 保存 Cookie 的函数
async def save_session():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context()

        page = await context.new_page()
        await page.goto("https://www.taobao.com")

        print("请手动登录淘宝，登录完成后按回车继续...")
        input()  # 等待用户确认登录完成

        # 保存 Cookies 到文件
        cookies = await context.cookies()
        with open("taobao_cookies.json", "w", encoding="utf-8") as f:
            json.dump(cookies, f, ensure_ascii=False, indent=4)

        print("Cookies 已保存到 taobao_cookies.json 文件！")
        await browser.close()

# 爬取商品信息的函数
async def scrape_taobao(query):
    results = []
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=False)
        context = await browser.new_context()

        # 加载 Cookie
        try:
            with open("taobao_cookies.json", "r") as f:
                cookies = json.load(f)
                await context.add_cookies(cookies)
        except FileNotFoundError:
            print("Cookie 文件不存在，请先运行 save_session() 保存会话！")
            await save_session()  # 调用保存 Cookie 的函数
            with open("taobao_cookies.json", "r") as f:
                cookies = json.load(f)
                await context.add_cookies(cookies)

        page = await context.new_page()
        await page.goto(f"https://s.taobao.com/search?q={query}", timeout=60000)

        # 等待页面加载
        await page.wait_for_selector('a.doubleCardWrapperAdapt--mEcC7olq', timeout=60000)

        # 模拟滚动页面，加载更多商品
        for _ in range(5):
            await page.mouse.wheel(0, 1000)
            await asyncio.sleep(1)

        # 提取商品信息
        items = await page.query_selector_all('a.doubleCardWrapperAdapt--mEcC7olq')  # 商品容器选择器

        for item in items:
            try:
                # 提取标题
                title_element = await item.query_selector('div.title--qJ7Xg_90 span')
                title = await title_element.inner_text() if title_element else "N/A"

                # 提取价格
                price_int_element = await item.query_selector('span.priceInt--yqqZMJ5a')
                price_float_element = await item.query_selector('span.priceFloat--XpixvyQ1')
                price_int = await price_int_element.inner_text() if price_int_element else "0"
                price_float = await price_float_element.inner_text() if price_float_element else ".00"
                price = f"{price_int}{price_float}"

                # 提取链接
                link = await item.get_attribute('href')

                # 提取图片链接
                img_element = await item.query_selector('img')  # 定位到 <img> 标签
                img_url = await img_element.get_attribute('src') if img_element else "N/A"

                # 保存结果
                results.append({
                    "title": title.strip(),
                    "price": price.strip(),
                    "link": "https:" + link.strip() if link.startswith("//") else link.strip(),
                    "image": "https:" + img_url.strip() if img_url.startswith("//") else img_url.strip()
                })
            except Exception as e:
                print(f"Error parsing item: {e}")

        print("爬取完成，返回商品信息。")
        await browser.close()

    return results

@app.route('/scrape', methods=['GET'])
def scrape():
    # 从 HTTP 请求中获取查询参数
    query = request.args.get('query')
    if not query:
        return jsonify({"error": "Query parameter 'query' is required"}), 400

    # 调用爬虫逻辑
    try:
        data = asyncio.run(scrape_taobao(query))  # 调用异步爬虫函数
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000)
