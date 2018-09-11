import youtube_dl

def fetch_data(link):
    with youtube_dl.YoutubeDL({'writeautomaticsub':True,'writesubtitles':True,}) as ydl:
        info_dict = ydl.extract_info(link, download=False)
    return info_dict