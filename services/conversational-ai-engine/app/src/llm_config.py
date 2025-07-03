from langchain_community.llms import Ollama
from langchain.callbacks.manager import CallbackManager
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler


class LLMConfig:
    def __init__(self):
        # 스트리밍 콜백 설정
        callback_manager = CallbackManager([StreamingStdOutCallbackHandler()])

        # Mistral 모델 - 대화/의도 파악용
        self.chat_llm = Ollama(
            model="mistral:7b-instruct",
            callback_manager=callback_manager,
            temperature=0.7
        )

        # CodeLlama 모델 - 코드 생성용
        self.code_llm = Ollama(
            model="codellama:13b-instruct",
            callback_manager=callback_manager,
            temperature=0.2  # 코드는 더 정확하게
        )

    def get_chat_model(self):
        return self.chat_llm

    def get_code_model(self):
        return self.code_llm


# # 사용 예시
# llm_config = LLMConfig()
# chat_model = llm_config.get_chat_model()
# response = chat_model.invoke("사용자가 쇼핑몰을 만들고 싶어합니다. 어떤 기능이 필요할까요?")