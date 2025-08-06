#ifndef LLAMA_WRAPPER_H
#define LLAMA_WRAPPER_H

#include <string>
#include <memory>

// Include real llama.cpp headers
#include "llama.h"

class LlamaWrapper {
public:
    LlamaWrapper();
    ~LlamaWrapper();
    
    // Model management
    bool loadModel(const std::string& modelPath);
    void unloadModel();
    bool isModelLoaded() const;
    
    // Context management
    bool createContext();
    void destroyContext();
    bool isContextCreated() const;
    
    // Text generation
    std::string generateText(const std::string& prompt, int maxTokens = 512);
    
    // Model information
    std::string getModelInfo() const;
    size_t getModelSize() const;
    
    // Parameter getters
    llama_model_params getModelParams() const { return m_modelParams; }
    llama_context_params getContextParams() const { return m_contextParams; }
    
    // Parameter setters
    void setModelParams(const llama_model_params& params) { m_modelParams = params; }
    void setContextParams(const llama_context_params& params) { m_contextParams = params; }

private:
    llama_model* m_model;
    llama_context* m_context;
    std::string m_modelPath;
    bool m_modelLoaded;
    bool m_contextCreated;
    
    // Default parameters
    llama_model_params m_modelParams;
    llama_context_params m_contextParams;
    
    // Helper functions
    void initializeDefaultParams();
    std::string generateIntelligentResponse(const std::string& userInput);
};

#endif // LLAMA_WRAPPER_H 