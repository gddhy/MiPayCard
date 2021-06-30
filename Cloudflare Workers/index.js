const gitHost = "https://raw.githubusercontent.com/gddhy/MiPayCard/master/"
const online_url = gitHost + "online_card.json"
const list_url = gitHost + "card_list.json"

async function gatherResponse(response) {
  const { headers } = response
  const contentType = headers.get("content-type") || ""
  if (contentType.includes("application/json")) {
    return JSON.stringify(await response.json())
  }
  else if (contentType.includes("application/text")) {
    return await response.text()
  }
  else if (contentType.includes("text/html")) {
    return await response.text()
  }
  else {
    return await response.text()
  }
}

async function handleRequest(type) {
  if(type == 0){
    return new Response("ERROR", { status: 200 })
  }
  const init = {
    headers: {
      "content-type": "application/json;charset=UTF-8",
    },
  }
  const response = await fetch(type == 1 ? online_url : list_url, init)
  const results = await gatherResponse(response)
  return new Response(results, init)
}

addEventListener("fetch", event => {
  const str = event.request.url
  var type = 0
  if(str.indexOf("?online=true")>=0){
    type = 1
  } else if(str.indexOf("?list=true")>=0){
    type = 2
  } 
  return event.respondWith(handleRequest(type))
})