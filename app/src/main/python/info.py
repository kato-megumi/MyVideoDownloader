import youtube_dl

def fetch_data(link):
    with youtube_dl.YoutubeDL({'writeautomaticsub':True,'writesubtitles':True,}) as ydl:
        try:
            info_dict = ydl.extract_info(link, download=False)
            if info_dict.get("_type"):
                info_dict["dltype"]=2
            else:
                info_dict["dltype"]=1
        except Exception as e:
            info_dict = {"dltype":0}
    return info_dict
def get_sub(info):
    if info["subtitles"] != {}:
        for i in info["subtitles"]["en"]:
            if i["ext"]=="vtt":
                return i["url"]
    else:
        for i in info["automatic_captions"]["en"]:
            if i["ext"]=="vtt":
                return i["url"]
    return ""